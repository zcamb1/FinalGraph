package com.samsung.iug.ui.preview

data class StepFile(
    val utterance: String,
    val steps: List<Step>
)

data class Step(
    val step_id: String,
    val nlg: String,
    val screen_id: String,
    val layout_match: List<LayoutMatch>,
    val action: String,
    val next_step_ids: List<String>
)

data class LayoutMatch(
    val text: String?,
    val className: String?,
    val resourceId: String?
)