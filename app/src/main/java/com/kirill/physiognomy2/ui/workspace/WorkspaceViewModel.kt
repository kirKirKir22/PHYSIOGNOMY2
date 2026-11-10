package com.kirill.physiognomy2.ui.workspace

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kirill.physiognomy2.data.PhysiognomyCatalog
import com.kirill.physiognomy2.data.Question
import com.kirill.physiognomy2.data.Step
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class PhotoItem(
    val path: String,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val grayscale: Boolean = false,
    val mirrored: Boolean = false,
)

data class Profile(
    val id: String,
    val name: String,
    val createdAt: Long,
    val answers: Map<String, String>,
)

class WorkspaceViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var stepNumber by mutableIntStateOf(1)
        private set

    var questionIndex by mutableIntStateOf(0)
        private set

    val answers = mutableStateMapOf<String, String>()

    val photos = mutableStateListOf<PhotoItem>()

    var currentPhotoIndex by mutableIntStateOf(0)
        private set

    var effectsEnabled by mutableStateOf(true)
        private set

    val profiles = mutableStateListOf<Profile>()

    val step: Step
        get() = PhysiognomyCatalog.step(stepNumber)

    val question: Question
        get() = step.questions[questionIndex.coerceIn(0, step.questions.lastIndex)]

    val currentPhoto: PhotoItem?
        get() = photos.getOrNull(currentPhotoIndex)

    val totalQuestions: Int
        get() = PhysiognomyCatalog.totalQuestions

    val answeredTotal: Int
        get() = answers.size

    val canGoBack: Boolean
        get() = questionIndex > 0 || stepNumber > 1

    val isLastQuestionOfStep: Boolean
        get() = questionIndex >= step.questions.lastIndex

    val isLastStep: Boolean
        get() = stepNumber >= PhysiognomyCatalog.steps.size

    init {
        load()
    }

    fun answeredInStep(number: Int = stepNumber): Int {
        val target = PhysiognomyCatalog.step(number)
        return target.questions.count { answers.containsKey(it.id) }
    }

    fun answerFor(questionId: String): String? = answers[questionId]

    fun selectOption(questionId: String, optionId: String) {
        answers[questionId] = optionId
        persistAnswers()
    }

    fun clearOption(questionId: String) {
        if (answers.remove(questionId) != null) persistAnswers()
    }

    fun openStep(number: Int) {
        stepNumber = number.coerceIn(1, PhysiognomyCatalog.steps.size)
        questionIndex = firstUnansweredIndex(stepNumber)
        persistNavigation()
    }

    fun focusQuestion(index: Int) {
        questionIndex = index.coerceIn(0, step.questions.lastIndex)
        persistNavigation()
    }

    fun nextQuestion() {
        if (!isLastQuestionOfStep) {
            questionIndex += 1
        } else if (!isLastStep) {
            stepNumber += 1
            questionIndex = 0
        }
        persistNavigation()
    }

    fun previousQuestion() {
        if (questionIndex > 0) {
            questionIndex -= 1
        } else if (stepNumber > 1) {
            stepNumber -= 1
            questionIndex = PhysiognomyCatalog.step(stepNumber).questions.lastIndex
        }
        persistNavigation()
    }

    private fun firstUnansweredIndex(number: Int): Int {
        val target = PhysiognomyCatalog.step(number)
        val index = target.questions.indexOfFirst { !answers.containsKey(it.id) }
        return if (index == -1) 0 else index
    }

    fun addPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val remaining = MAX_PHOTOS - photos.size
        if (remaining <= 0) return
        val accepted = uris.take(remaining)
        val app = getApplication<Application>()
        viewModelScope.launch {
            val dir = File(app.filesDir, "photos").apply { mkdirs() }
            val added = mutableListOf<PhotoItem>()
            withContext(Dispatchers.IO) {
                accepted.forEachIndexed { index, uri ->
                    try {
                        val mime = app.contentResolver.getType(uri)
                        val ext = when (mime) {
                            "image/png" -> "png"
                            "image/webp" -> "webp"
                            else -> "jpg"
                        }
                        val file = File(dir, "photo_${System.currentTimeMillis()}_$index.$ext")
                        app.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output -> input.copyTo(output) }
                        }
                        if (file.exists() && file.length() > 0) {
                            added += PhotoItem(path = file.absolutePath)
                        }
                    } catch (_: Exception) {
                    }
                }
            }
            if (added.isNotEmpty()) {
                val startIndex = photos.size
                photos += added
                currentPhotoIndex = startIndex.coerceIn(0, photos.lastIndex)
                persistPhotos()
            }
        }
    }

    fun selectPhoto(index: Int) {
        if (photos.isEmpty()) return
        val target = index.coerceIn(0, photos.lastIndex)
        if (target == currentPhotoIndex) return
        currentPhotoIndex = target
    }

    fun deleteCurrentPhoto() {
        val photo = currentPhoto ?: return
        photos.remove(photo)
        File(photo.path).delete()
        currentPhotoIndex = currentPhotoIndex.coerceIn(0, (photos.size - 1).coerceAtLeast(0))
        persistPhotos()
    }

    fun setBrightness(value: Float) = updateCurrentPhoto { it.copy(brightness = value.coerceIn(-1f, 1f)) }

    fun setContrast(value: Float) = updateCurrentPhoto { it.copy(contrast = value.coerceIn(0.3f, 2.5f)) }

    fun toggleGrayscale() = updateCurrentPhoto { it.copy(grayscale = !it.grayscale) }

    fun toggleMirror() = updateCurrentPhoto { it.copy(mirrored = !it.mirrored) }

    fun resetPhotoEffects() = updateCurrentPhoto { PhotoItem(it.path) }

    fun toggleEffects() {
        effectsEnabled = !effectsEnabled
        prefs.edit().putBoolean(KEY_EFFECTS, effectsEnabled).apply()
    }

    private inline fun updateCurrentPhoto(transform: (PhotoItem) -> PhotoItem) {
        val index = currentPhotoIndex
        val photo = photos.getOrNull(index) ?: return
        photos[index] = transform(photo)
        persistPhotos()
    }

    fun saveCurrentProfile(name: String? = null): Profile {
        val profile = Profile(
            id = UUID.randomUUID().toString(),
            name = name?.takeIf { it.isNotBlank() } ?: "Профиль ${profiles.size + 1}",
            createdAt = System.currentTimeMillis(),
            answers = answers.toMap(),
        )
        profiles.add(0, profile)
        persistProfiles()
        return profile
    }

    fun deleteProfile(id: String) {
        profiles.removeAll { it.id == id }
        persistProfiles()
    }

    fun resetAnswers() {
        answers.clear()
        stepNumber = 1
        questionIndex = 0
        persistAnswers()
        persistNavigation()
    }

    fun deleteAllPhotos() {
        photos.forEach { File(it.path).delete() }
        photos.clear()
        currentPhotoIndex = 0
        persistPhotos()
    }

    private fun load() {
        stepNumber = prefs.getInt(KEY_STEP, 1).coerceIn(1, PhysiognomyCatalog.steps.size)
        questionIndex = prefs.getInt(KEY_QUESTION, 0).coerceAtLeast(0)
        effectsEnabled = prefs.getBoolean(KEY_EFFECTS, true)

        runCatching {
            val obj = JSONObject(prefs.getString(KEY_ANSWERS, null) ?: "{}")
            obj.keys().forEach { key -> answers[key] = obj.getString(key) }
        }

        runCatching {
            val array = JSONArray(prefs.getString(KEY_PHOTOS, null) ?: "[]")
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val path = item.getString("path")
                if (File(path).exists()) {
                    photos += PhotoItem(
                        path = path,
                        brightness = item.optDouble("brightness", 0.0).toFloat(),
                        contrast = item.optDouble("contrast", 1.0).toFloat(),
                        grayscale = item.optBoolean("grayscale", false),
                        mirrored = item.optBoolean("mirrored", false),
                    )
                }
            }
        }

        runCatching {
            val array = JSONArray(prefs.getString(KEY_PROFILES, null) ?: "[]")
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val answerObject = item.optJSONObject("answers") ?: JSONObject()
                val map = mutableMapOf<String, String>()
                answerObject.keys().forEach { key -> map[key] = answerObject.getString(key) }
                profiles += Profile(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    createdAt = item.optLong("createdAt", 0L),
                    answers = map,
                )
            }
        }

        questionIndex = questionIndex.coerceIn(0, step.questions.lastIndex)
    }

    private fun persistNavigation() {
        prefs.edit()
            .putInt(KEY_STEP, stepNumber)
            .putInt(KEY_QUESTION, questionIndex)
            .apply()
    }

    private fun persistAnswers() {
        val obj = JSONObject()
        answers.forEach { (key, value) -> obj.put(key, value) }
        prefs.edit().putString(KEY_ANSWERS, obj.toString()).apply()
    }

    private fun persistPhotos() {
        val array = JSONArray()
        photos.forEach { photo ->
            array.put(
                JSONObject()
                    .put("path", photo.path)
                    .put("brightness", photo.brightness.toDouble())
                    .put("contrast", photo.contrast.toDouble())
                    .put("grayscale", photo.grayscale)
                    .put("mirrored", photo.mirrored),
            )
        }
        prefs.edit().putString(KEY_PHOTOS, array.toString()).apply()
    }

    private fun persistProfiles() {
        val array = JSONArray()
        profiles.forEach { profile ->
            val answerObject = JSONObject()
            profile.answers.forEach { (key, value) -> answerObject.put(key, value) }
            array.put(
                JSONObject()
                    .put("id", profile.id)
                    .put("name", profile.name)
                    .put("createdAt", profile.createdAt)
                    .put("answers", answerObject),
            )
        }
        prefs.edit().putString(KEY_PROFILES, array.toString()).apply()
    }

    private companion object {
        const val PREFS_NAME = "physiognomy_state"
        const val KEY_STEP = "step"
        const val KEY_QUESTION = "question"
        const val KEY_ANSWERS = "answers"
        const val KEY_PHOTOS = "photos"
        const val KEY_PROFILES = "profiles"
        const val KEY_EFFECTS = "effects"
        const val MAX_PHOTOS = 10
    }
}
