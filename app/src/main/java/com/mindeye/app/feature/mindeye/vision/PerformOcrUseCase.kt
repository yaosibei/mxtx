package com.mindeye.app.feature.mindeye.vision

import com.mindeye.app.core.model.OcrResult
import com.mindeye.app.core.model.Rect
import com.mindeye.app.core.model.TextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformOcrUseCase @Inject constructor() {

    private val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    suspend operator fun invoke(imageBitmap: android.graphics.Bitmap): OcrResult {
        val inputImage = InputImage.fromBitmap(imageBitmap, 0)
        val visionText = recognizer.process(inputImage).await()

        val fullText = visionText.text
        val blocks = visionText.textBlocks.map { block ->
            TextBlock(
                text = block.text,
                confidence = 1.0f, // ML Kit TextBlock doesn't provide confidence directly at this level
                boundingBox = block.boundingBox?.let { rect ->
                    Rect(
                        left = rect.left,
                        top = rect.top,
                        right = rect.right,
                        bottom = rect.bottom
                    )
                }
            )
        }

        return OcrResult(
            text = fullText,
            blocks = blocks
        )
    }

    fun close() {
        recognizer.close()
    }
}
