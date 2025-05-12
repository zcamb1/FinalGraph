package com.samsung.iug.ui.preview

import com.google.gson.Gson
import java.io.File

object StepRunner {
    fun runStepsFromJson(jsonPath: String, device: String) {
        val file = File(jsonPath)
        val json = file.readText()
        val stepFile = Gson().fromJson(json, StepFile::class.java)

        val stepMap = stepFile.steps.associateBy { it.step_id }

        fun runStep(stepId: String) {
            println("▶️ Running step: $stepId")
            val step = stepMap[stepId]
            if (step == null) {
                println("❌ Step not found: $stepId")
                return
            }

            println("➡️ Opening screen: ${step.screen_id}")
            openScreen(device, step.screen_id)

            println("⏳ Waiting 5 seconds for layout to load...")
            Thread.sleep(5000)

            println("📦 Parsing layout...")
            val viewRoots = LayoutParser.parse(device)
            if (viewRoots == null) {
                println("❌ Failed to parse layout.")
                return
            }

            val allViews = viewRoots.flatMap { it.flatten() }

            println("🔍 Searching for match from layout_match list...")
            val match = allViews.find { view ->
                step.layout_match.any { match ->
                    view.text == match.text &&
                            view.className == match.className &&
                            view.resourceId == match.resourceId
                }
            }

            if (match != null) {
                println("✅ Found matching view: ${match.text}, ${match.resourceId}")
                ScreenMirror.highlight(match.bounds)
            } else {
                println("⚠️ No matching view found for step $stepId")
            }

            for (nextId in step.next_step_ids) {
                runStep(nextId)
            }
        }

        runStep(stepFile.steps.first().step_id)
    }

    private fun openScreen(device: String, screenId: String) {
        val cmd = "adb -s $device shell am start -n $screenId"
        println("📲 Executing: $cmd")
        Runtime.getRuntime().exec(cmd).waitFor()
    }
}
