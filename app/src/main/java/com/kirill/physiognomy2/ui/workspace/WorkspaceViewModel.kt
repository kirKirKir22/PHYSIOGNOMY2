package com.kirill.physiognomy2.ui.workspace

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
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

    var photoNotice by mutableStateOf<String?>(null)
        private set

    var activeProfileId by mutableStateOf<String?>(null)
        private set

    var activeProfileName by mutableStateOf<String?>(null)
        private set

    val isAtPhotoLimit: Boolean
        get() = photos.size >= MAX_PHOTOS

    val photosRemaining: Int
        get() = (MAX_PHOTOS - photos.size).coerceAtLeast(0)

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
        } else {
            stepNumber = PhysiognomyCatalog.steps.size
            questionIndex = step.questions.lastIndex
        }
        persistNavigation()
    }

    fun skipQuestion() {
        nextQuestion()
    }

    fun goToLastStep() {
        stepNumber = PhysiognomyCatalog.steps.size
        questionIndex = PhysiognomyCatalog.step(stepNumber).questions.lastIndex
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
        val remaining = photosRemaining
        if (remaining <= 0) {
            photoNotice = "Достигнут лимит: $MAX_PHOTOS фото. Удалите лишние, чтобы добавить новые."
            return
        }
        val accepted = uris.take(remaining)
        if (accepted.size < uris.size) {
            photoNotice = "Добавлено ${accepted.size} из ${uris.size}: лимит $MAX_PHOTOS фото."
        }
        val app = getApplication<Application>()
        viewModelScope.launch {
            val dir = File(app.filesDir, "photos").apply { mkdirs() }
            val added = mutableListOf<PhotoItem>()
            var failed = 0
            withContext(Dispatchers.IO) {
                accepted.forEachIndexed { index, uri ->
                    val file = File(dir, "photo_${System.currentTimeMillis()}_$index.jpg")
                    val ok = runCatching {
                        importPhoto(app.contentResolver, uri, file)
                    }.getOrDefault(false)
                    if (ok && file.exists() && file.length() > 0) {
                        added += PhotoItem(path = file.absolutePath)
                    } else {
                        file.delete()
                        failed += 1
                    }
                }
            }
            if (added.isNotEmpty()) {
                val startIndex = photos.size
                photos += added
                currentPhotoIndex = startIndex.coerceIn(0, photos.lastIndex)
                persistPhotos()
            }
            if (failed > 0) {
                photoNotice = if (added.isEmpty()) {
                    "Не удалось загрузить фото"
                } else {
                    "Не удалось загрузить $failed из ${accepted.size} фото"
                }
            }
        }
    }

    fun consumePhotoNotice() {
        photoNotice = null
    }

    fun showPhotoNotice(message: String) {
        photoNotice = message
    }

    private fun importPhoto(resolver: ContentResolver, uri: Uri, target: File): Boolean {
        val bitmap = decodeSampledBitmap(resolver, uri, MAX_PHOTO_SIDE) ?: return false
        val oriented = applyExifRotation(resolver, uri, bitmap)
        val saved = runCatching {
            target.outputStream().use { output ->
                oriented.compress(Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, output)
            }
        }.getOrDefault(false)
        if (oriented !== bitmap) oriented.recycle()
        bitmap.recycle()
        return saved
    }

    private fun decodeSampledBitmap(
        resolver: ContentResolver,
        uri: Uri,
        maxSide: Int,
    ): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxSide || bounds.outHeight / (sample * 2) >= maxSide) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        val longest = maxOf(decoded.width, decoded.height)
        if (longest <= maxSide) return decoded
        val ratio = maxSide.toFloat() / longest
        val scaled = Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * ratio).toInt().coerceAtLeast(1),
            (decoded.height * ratio).toInt().coerceAtLeast(1),
            true,
        )
        if (scaled !== decoded) decoded.recycle()
        return scaled
    }

    private fun applyExifRotation(resolver: ContentResolver, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            resolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }
        return runCatching {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }.getOrDefault(bitmap)
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

    fun deletePhotoAt(index: Int) {
        val photo = photos.getOrNull(index) ?: return
        photos.removeAt(index)
        File(photo.path).delete()
        currentPhotoIndex = currentPhotoIndex.coerceIn(0, (photos.size - 1).coerceAtLeast(0))
        persistPhotos()
    }

    fun setBrightness(value: Float) {
        ensureEffectsEnabled()
        updateCurrentPhoto(persist = false) { it.copy(brightness = value.coerceIn(-1f, 1f)) }
    }

    fun setContrast(value: Float) {
        ensureEffectsEnabled()
        updateCurrentPhoto(persist = false) { it.copy(contrast = value.coerceIn(0.3f, 2.5f)) }
    }

    fun commitPhotoEdits() = persistPhotos()

    fun toggleGrayscale() {
        ensureEffectsEnabled()
        updateCurrentPhoto { it.copy(grayscale = !it.grayscale) }
    }

    fun toggleMirror() {
        ensureEffectsEnabled()
        updateCurrentPhoto { it.copy(mirrored = !it.mirrored) }
    }

    fun resetPhotoEffects() {
        ensureEffectsEnabled()
        updateCurrentPhoto { PhotoItem(it.path) }
    }

    fun toggleEffects() {
        effectsEnabled = !effectsEnabled
        prefs.edit().putBoolean(KEY_EFFECTS, effectsEnabled).apply()
    }

    private fun ensureEffectsEnabled() {
        if (!effectsEnabled) {
            effectsEnabled = true
            prefs.edit().putBoolean(KEY_EFFECTS, true).apply()
        }
    }

    private inline fun updateCurrentPhoto(
        persist: Boolean = true,
        transform: (PhotoItem) -> PhotoItem,
    ) {
        val index = currentPhotoIndex
        val photo = photos.getOrNull(index) ?: return
        photos[index] = transform(photo)
        if (persist) persistPhotos()
    }

    fun saveCurrentProfile(name: String? = null): Profile {
        val chosenName = name?.trim().orEmpty()
        val existing = profiles.firstOrNull { it.id == activeProfileId }

        if (existing != null) {
            val updated = existing.copy(
                name = chosenName.ifBlank { existing.name },
                createdAt = System.currentTimeMillis(),
                answers = answers.toMap(),
            )
            val index = profiles.indexOf(existing)
            if (index >= 0) profiles[index] = updated
            persistProfiles()
            return updated
        }

        val profile = Profile(
            id = UUID.randomUUID().toString(),
            name = chosenName.ifBlank { defaultProfileName() },
            createdAt = System.currentTimeMillis(),
            answers = answers.toMap(),
        )
        profiles.add(0, profile)
        persistProfiles()
        return profile
    }

    private fun defaultProfileName(): String {
        val numbers = profiles.mapNotNull { profile ->
            PROFILE_NAME_PATTERN.find(profile.name)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
        return "Профиль ${(numbers.maxOrNull() ?: 0) + 1}"
    }

    fun loadProfile(profile: Profile) {
        answers.clear()
        answers.putAll(profile.answers)
        activeProfileId = profile.id
        activeProfileName = profile.name
        stepNumber = 1
        questionIndex = 0
        prefs.edit().putString(KEY_ACTIVE_PROFILE, profile.id).apply()
        persistAnswers()
        persistNavigation()
    }

    fun renameActiveProfile(name: String) {
        activeProfileName = name.trim().takeIf { it.isNotBlank() }
        val id = activeProfileId ?: return
        val existing = profiles.firstOrNull { it.id == id } ?: return
        val updated = existing.copy(name = activeProfileName ?: existing.name)
        val index = profiles.indexOf(existing)
        if (index >= 0) profiles[index] = updated
        persistProfiles()
    }

    fun startNewProfile() {
        activeProfileId = null
        activeProfileName = null
        prefs.edit().remove(KEY_ACTIVE_PROFILE).apply()
    }

    fun deleteProfile(id: String) {
        profiles.removeAll { it.id == id }
        if (activeProfileId == id) {
            activeProfileId = null
            activeProfileName = null
            prefs.edit().remove(KEY_ACTIVE_PROFILE).apply()
        }
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

        val savedProfileId = prefs.getString(KEY_ACTIVE_PROFILE, null)
        if (savedProfileId != null) {
            val profile = profiles.firstOrNull { it.id == savedProfileId }
            if (profile != null) {
                activeProfileId = profile.id
                activeProfileName = profile.name
            } else {
                prefs.edit().remove(KEY_ACTIVE_PROFILE).apply()
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
        const val KEY_ACTIVE_PROFILE = "active_profile"
        const val MAX_PHOTOS = 10
        const val MAX_PHOTO_SIDE = 1800
        const val PHOTO_QUALITY = 88
        val PROFILE_NAME_PATTERN = Regex("""Профиль (\d+)""")
    }
}
