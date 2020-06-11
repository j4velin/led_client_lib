package de.j4velin.ledclient.lib

import android.R
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import de.j4velin.lib.colorpicker.ColorPickerDialog
import de.j4velin.lib.colorpicker.ColorPreviewButton
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

interface Callback {
    fun configurationResult(effect: LedEffect)
}

private fun dpToPx(dp: Float, context: Context): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
        .toInt()

class UiDialog(
    private val callback: Callback,
    context: Context,
    private val currentEffect: LedEffect?
) :
    Dialog(context, R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar) {

    private val valueViews: MutableMap<String, View> = HashMap()
    private val padding = dpToPx(10f, context)

    private fun init() {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        val table = TableLayout(context)
        table.setPadding(padding, padding, padding, padding)
        val spinner = Spinner(context)

        val effects = LedEffect.getEffects()
        val spinnerRow = getSpinnerRow(spinner, effects.map { it.simpleName!! })

        table.addView(spinnerRow)

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                table.removeAllViews()
                table.addView(spinnerRow)
                val effectClass = effects[position]
                getEffectRows(effectClass).forEach(table::addView)
                if (currentEffect != null && currentEffect::class == effectClass) {
                    fillFromLedEffect(currentEffect)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val index = currentEffect?.let { effects.indexOf(it::class) } ?: 0
        spinner.setSelection(index)

        layout.addView(table)

        val button = Button(context)
        button.text = "Save"
        button.setOnClickListener {
            try {
                callback.configurationResult(toLedEffect(effects[spinner.selectedItemPosition].simpleName!!))
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Invalid configuration: ${e.message ?: e::class.java.simpleName}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        layout.addView(button)

        setContentView(layout)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    /**
     * Constructs a LedEffect object from the views configuration values
     */
    private fun toLedEffect(name: String): LedEffect {
        val json = JsonObject()
        valueViews.mapValues {
            val view = it.value
            if (view is CheckBox) {
                JsonPrimitive(view.isChecked)
            } else if (view is EditText) {
                if (view.text.toString().isEmpty()) {
                    throw java.lang.IllegalArgumentException("Property '${it.key}' is missing")
                }
                if (view.inputType and InputType.TYPE_CLASS_NUMBER == 1) {
                    if (view.inputType and InputType.TYPE_NUMBER_FLAG_DECIMAL == 1) {
                        JsonPrimitive(view.text.toString().toDouble())
                    } else {
                        JsonPrimitive(view.text.toString().toLong())
                    }
                } else {
                    JsonPrimitive(view.text.toString())
                }
            } else if (view is ColorPreviewButton) {
                colorToArray(view.color)
            } else {
                throw IllegalArgumentException("Unknown view type: ${view::class.java.name}")
            }
        }.forEach {
            json.add(it.key, it.value)
        }
        return LedEffect.fromJson(name, json)
    }

    /**
     * Fills the views with the given effect's values
     */
    private fun fillFromLedEffect(effect: LedEffect) {
        val json = effect.toJSON()
        valueViews.forEach {
            when (val view = it.value) {
                is CheckBox -> view.isChecked = json[it.key].asBoolean
                is EditText -> view.setText(json[it.key].asString)
                is ColorPreviewButton -> view.color = arrayToColor(json[it.key].asJsonArray)
                else -> throw IllegalArgumentException("Unknown view type: ${view::class.java.name}")
            }
        }
    }

    /**
     * Creates a table row for an effect selection spinner
     */
    private fun getSpinnerRow(spinner: Spinner, values: Collection<String>): TableRow {
        val row = TableRow(context)
        val spinnerAdapter =
            ArrayAdapter(context, R.layout.simple_spinner_item, values.toTypedArray())
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        val label = TextView(context)
        label.text = "Effect:"
        row.addView(label)
        row.addView(spinner)
        return row
    }

    /**
     * Creates a table row for the given effect class
     */
    private fun getEffectRows(effectClass: KClass<out LedEffect>): List<TableRow> {
        val rows = ArrayList<TableRow>(effectClass.declaredMembers.size)
        valueViews.clear()
        for (f in effectClass.declaredMemberProperties) {
            val row = TableRow(context)
            val label = TextView(context)
            label.text = "${f.name}:"
            row.addView(label)

            val edit: View
            if (f.name.equals("color", true)) {
                row.setPadding(0, padding, 0, padding)
                edit = ColorPreviewButton(context)
                val params = edit.layoutParams ?: TableRow.LayoutParams()
                params.height = dpToPx(20f, context)
                edit.layoutParams = params
                edit.color = Color.RED
                edit.setOnClickListener {
                    val dialog = ColorPickerDialog(context, edit.color)
                    dialog.alphaSliderVisible = false
                    dialog.hexValueEnabled = false
                    dialog.setOnColorChangedListener {
                        edit.color = it
                    }
                    dialog.show()
                }
            } else if (f.returnType::class == Boolean::class) {
                edit = CheckBox(context)
            } else {
                edit = EditText(context)
                if (isNumberType(f.returnType)) {
                    edit.inputType = if (isFloatingPoint(f.returnType)) {
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                    } else {
                        InputType.TYPE_CLASS_NUMBER
                    }
                }
            }

            valueViews[f.name] = edit

            row.addView(edit)
            rows.add(row)
        }

        return rows
    }

    private fun isFloatingPoint(c: KType) =
        c == Float::class.createType() || c == Double::class.createType()

    @OptIn(ExperimentalStdlibApi::class)
    private fun isNumberType(type: KType) = type.isSubtypeOf(typeOf<Number>())
}


