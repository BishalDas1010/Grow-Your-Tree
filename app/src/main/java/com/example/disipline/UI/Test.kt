package com.example.disipline.UI

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode

@Composable
fun ForestGrowthView(progress: Float, modifier: Modifier = Modifier) {
    var modelNodeState by remember { mutableStateOf<ModelNode?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            SceneView(context).apply {
                cameraNode.position = Position(x = 0f, y = 0.5f, z = 2.8f)

                try {
                    val modelInstance =
                        modelLoader.createModelInstance("models/island_growth.glb")
                    val modelNode = ModelNode(
                        modelInstance = modelInstance,
                        autoAnimate = false,
                        scaleToUnits = 1.2f
                    )
                    addChildNode(modelNode)
                    modelNodeState = modelNode
                } catch (e: Exception) {
                    Log.e("ForestGrowth", "Error loading model: ${e.message}", e)
                }
            }
        },
        update = { _ ->
            modelNodeState?.modelInstance?.animator?.let { animator ->
                if (animator.animationCount > 0) {
                    val duration = animator.getAnimationDuration(0)
                    animator.applyAnimation(0, progress.coerceIn(0f, 1f) * duration)
                    animator.updateBoneMatrices()
                } else {
                    Log.w(
                        "ForestGrowth",
                        "GLB has no animation - re-export with Animation enabled"
                    )
                }
            }
        }
    )
}