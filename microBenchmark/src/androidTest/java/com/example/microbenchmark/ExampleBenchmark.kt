package com.example.microbenchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.composepractice.MainActivity
import com.example.composepractice.ui.project.counter.Counter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun benchmarkMyComposable() {

        benchmarkRule.measureRepeated {
            composeTestRule.setContent {
                Counter(
                    start = 99,
                    end = 1
                )
            }
        }
    }
}