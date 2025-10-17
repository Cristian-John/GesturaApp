package com.example.gesturaapp

import android.content.Context
import ai.onnxruntime.*
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer

class OnnxModelHelper(context: Context) {
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val inputName: String

    init {
        // Load model from assets
        val modelFile = loadModelFile(context, "gesture_model.onnx")
        session = env.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
        inputName = session.inputNames.first() // Auto-detect first input name
    }

    private fun loadModelFile(context: Context, modelName: String): File {
        val file = File(context.filesDir, modelName)
        if (!file.exists()) {
            context.assets.open(modelName).use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file
    }

    /**
     * Runs inference on the model.
     * @param inputData FloatArray containing input values.
     * @param inputShape LongArray describing the shape of the input (e.g. [1, 10]).
     * @return List of output results.
     */
    fun runInference(inputData: FloatArray, inputShape: LongArray): List<Any?> {
        val floatBuffer = FloatBuffer.wrap(inputData)
        val inputTensor = OnnxTensor.createTensor(env, floatBuffer, inputShape)

        session.use { sess ->
            val output = sess.run(mapOf(inputName to inputTensor))
            return output.map { it.value }
        }
    }

    fun close() {
        session.close()
        env.close()
    }
}
