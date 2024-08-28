@file:OptIn(ExperimentalSharedTransitionApi::class)
@file:Suppress(
  "AnimateAsStateLabel", "UpdateTransitionLabel",
  "TransitionPropertiesLabel"
)

package me.chachako.sharedelement.bug

import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.delay
import me.chachako.sharedelement.bug.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (!Settings.canDrawOverlays(this)) {
      startActivity(
        Intent(
          Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
          Uri.parse("package:$packageName")
        )
      )
    }
  }

  override fun onResume() {
    super.onResume()
    if (Settings.canDrawOverlays(this)) {
      val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
      )
      windowManager.addView(
        ComposeView(this).also {
          it.setViewTreeLifecycleOwner(this)
          it.setViewTreeViewModelStoreOwner(this)
          it.setViewTreeSavedStateRegistryOwner(this)
          it.setContent {
            MyApplicationTheme {
              Content(windowManager, it)
            }
          }
        },
        layoutParams
      )
    }
  }
}

@Composable
private fun Content(
  parent: WindowManager,
  composeView: ComposeView,
) {
  var phase by remember { mutableStateOf(Phase.First) }
  val transition = updateTransition(phase)


  if (phase == Phase.Third) LaunchedEffect(Unit) {
    delay(600)
    parent.removeView(composeView)
  }

  Column(modifier = Modifier.fillMaxSize()) {
    val color by transition.animateColor {
      when (it) {
        Phase.First -> Color.Red
        Phase.Second -> Color.Blue
        Phase.Third -> Color.Green
      }
    }

    val scale by transition.animateFloat {
      when (it) {
        Phase.First -> 1f
        Phase.Second -> 1.1f
        Phase.Third -> 0.8f
      }
    }

    SharedTransitionLayout {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .background(color)
          .graphicsLayer { scaleX = scale; scaleY = scale }
      ) {
        androidx.compose.animation.AnimatedVisibility(
          visible = phase == Phase.First,
          enter = EnterTransition.None,
          exit = ExitTransition.None,
          modifier = Modifier.align(Alignment.Center)
        ) {
          SharedText(
            text = "First",
            animatedVisibilityScope = this,
          )
        }

        androidx.compose.animation.AnimatedVisibility(
          visible = phase == Phase.Second,
          enter = EnterTransition.None,
          exit = ExitTransition.None,
        ) {
          SharedText(
            text = "Second",
            animatedVisibilityScope = this,
            modifier = Modifier.graphicsLayer {
              alpha = 0.8f
              translationX = 200f
              translationY = 300f
            }
          )
        }
      }
    }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(40.dp)
    ) {
      Button(
        enabled = phase == Phase.First,
        onClick = { phase = Phase.Second }
      ) {
        Text(text = "begin transition")
      }
      Button(
        enabled = phase == Phase.Second,
        onClick = { phase = Phase.Third }
      ) {
        Text(text = "stop")
      }
    }
  }
}

@Composable
fun SharedTransitionScope.SharedText(
  text: String,
  animatedVisibilityScope: AnimatedVisibilityScope,
  modifier: Modifier = Modifier,
) = Text(
  text = text,
  fontSize = 38.sp,
  fontWeight = FontWeight.ExtraBold,
  color = Color.White,
  modifier = modifier.sharedBounds(
    rememberSharedContentState(key = 0),
    animatedVisibilityScope = animatedVisibilityScope,
    boundsTransform = { _, _ ->
      tween(durationMillis = 10000)
    }
  )
)

enum class Phase {
  First,
  Second,
  Third,
}
