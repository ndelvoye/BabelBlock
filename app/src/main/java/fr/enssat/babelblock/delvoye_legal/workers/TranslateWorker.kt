package fr.enssat.babelblock.delvoye_legal.workers

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.enssat.babelblock.delvoye_legal.tools.BlockService
import timber.log.Timber
import java.util.*

class TranslateWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val appContext = applicationContext
        val service = BlockService(appContext)

        var outputData: Data = Data.EMPTY
        Timber.d("inputData = %s", inputData.toString())
        return try {
            service.translator(
                Locale(inputData.getString("from").toString()),
                Locale(inputData.getString("to").toString()),
            ).translate(
                inputData.getString("sentenceToTranslate").toString()
            ) { sentenceTranslated ->
                outputData =
                    Data.Builder().putString("sentenceTranslated", sentenceTranslated).build()
                Result.success(outputData)
            }
            Timber.d("sentenceTranslated = %s", outputData)
            Result.success(outputData)
        } catch (throwable: Throwable) {
            Timber.e("Error while translating")
            Result.failure()
        }
    }
}