package com.example.composepractice

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import io.github.openflocon.flocon.Flocon
import io.github.openflocon.flocon.FloconLogger
import io.github.openflocon.flocon.plugins.analytics.analytics
import io.github.openflocon.flocon.plugins.dashboard.dashboard
import io.github.openflocon.flocon.plugins.dashboard.dsl.button
import io.github.openflocon.flocon.plugins.dashboard.dsl.form
import io.github.openflocon.flocon.plugins.dashboard.dsl.section
import io.github.openflocon.flocon.plugins.dashboard.dsl.text
import io.github.openflocon.flocon.plugins.dashboard.dsl.textField

@HiltAndroidApp
class BaseApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Flocon.initialize(this)
        FloconLogger.enabled = true

        Flocon.dashboard(id = "main") {
            section(name = "Tmp section") {
                button(
                    text = "tmp section button", id = "tmp_section_button", onClick = {
                        Log.e("TAG", "onCreate: button clicked", )
                })
            }

            form(
                name = "Test form",
                submitText = "Submit form",
                onSubmitted = { values ->
                    Log.e("TAG", "onCreate:")
                    values.forEach { (key, value) ->
                        Log.e("TAG", "$key - $value")
                    }
                }
            ) {
                text(label = "Test text", value = "Test value")
                textField(
                    label = "Test text field",
                    placeHolder = "placeholder",
                    id = "test_text_field",
                    value = "",
                    onSubmitted = {
                        Flocon.analytics("test_text_field")
                    }
                )
            }
        }
    }
}