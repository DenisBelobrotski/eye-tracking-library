package com.denisbelobrotski.eye_tracking_library.cv_util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.denisbelobrotski.eye_tracking_library.util.FileSystemUtils
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.objdetect.CascadeClassifier
import org.opencv.osgi.OpenCVNativeLoader
import java.io.File
import java.io.IOException
import kotlin.math.sqrt

class OpenCvUtils {
    companion object {
        private const val ShouldShowCachedCascadesForUser = true

        fun loadLibraries() {
            val loader = OpenCVNativeLoader()
            loader.init()
        }

        fun loadCascadeFromAssets(context: Context, assetPath: String): CascadeClassifier? {
            val faceCascadeAssetUri = Uri.parse(assetPath)
            val cachedFaceCascadeFile =
                FileSystemUtils.cacheAssetFile(context, faceCascadeAssetUri,
                        false, ShouldShowCachedCascadesForUser)
            var loadedCascade: CascadeClassifier? = null
            cachedFaceCascadeFile?.let {
                loadedCascade =
                    loadCascade(cachedFaceCascadeFile)
            }

            return loadedCascade
        }

        fun loadCascade(cascadeFile: File): CascadeClassifier {
            val faceCascadeClassifier = CascadeClassifier(cascadeFile.absolutePath)
            val isEmpty = faceCascadeClassifier.empty()

            if (isEmpty) {
                throw IOException("Cascade classifier is empty")
            }

            return faceCascadeClassifier
        }

        fun loadUserMat(context: Context, uri: Uri): Mat? {
            var resultMat: Mat? = null

            val resultBitmap =
                FileSystemUtils.loadUserBitmap(context, uri)
            resultBitmap?.let {
                // TODO: estimate bitmapToMat time
                resultMat = Mat()
                Utils.bitmapToMat(resultBitmap, resultMat)
            }

            return resultMat
        }

        fun emptyClone(sourceMat: Mat): Mat {
            return Mat(sourceMat.rows(), sourceMat.cols(), sourceMat.type())
        }

        fun getMatCenter(mat: Mat): Point {
            return Point((mat.cols() / 2).toDouble(), (mat.rows() / 2).toDouble())
        }

        fun getRectCenter(rect: Rect): Point {
            val center = Point(rect.x.toDouble(), rect.y.toDouble())
            center.x += rect.width * 0.5
            center.y += rect.height * 0.5

            return center
        }

        fun getBitmapFromMat(mat: Mat, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
            val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), config)
            Utils.matToBitmap(mat, bitmap)

            return bitmap
        }

        fun getDifference(point1: Point, point2: Point): Point {
            val point = Point()
            point.x = point1.x - point2.x
            point.y = point1.y - point2.y

            return point
        }

        fun getSquaredLength(point: Point): Double {
            return point.x * point.x + point.y * point.y
        }

        fun getLength(point: Point): Double {
            return sqrt(getSquaredLength(point))
        }

        fun normalize(point: Point) {
            val length = getLength(point)
            point.x /= length
            point.y /= length
        }
    }
}
