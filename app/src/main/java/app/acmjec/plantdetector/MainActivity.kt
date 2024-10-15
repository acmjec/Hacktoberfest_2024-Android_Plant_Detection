package app.acmjec.plantdetector

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import app.acmjec.plantdetector.ui.theme.GeminiChatBotTheme
import app.acmjec.plantdetector.ui.theme.Green
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

/**
 * @author Owais Raza
 */


class MainActivity : ComponentActivity() {

    private val uriState = MutableStateFlow("")
    private var cameraUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                uriState.update { uri.toString() }
            }
        }


    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                cameraUri?.let {
                    uriState.update { it.toString() }
                }
            }
        }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a temporary file for the captured image
        val tempFile = File.createTempFile("camera_image", ".jpg", cacheDir).apply {
            deleteOnExit()
        }
        cameraUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", tempFile)

        setContent {
            GeminiChatBotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary)
                                    .height(35.dp)
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.TopStart),
                                    text = stringResource(id = R.string.app_name),
                                    fontSize = 19.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    ) {
                        ChatScreen(paddingValues = it)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen(paddingValues: PaddingValues) {
        val chaViewModel = viewModel<ChatViewModel>()
        val chatState = chaViewModel.chatState.collectAsState().value

        val bitmap = getBitmap()

        var cameraPermission by remember { mutableStateOf(isPermissionGranted(this, android.Manifest.permission.CAMERA)) }
        var galleryPermission by remember { mutableStateOf(isPermissionGranted(this, if (Build.VERSION.SDK_INT < 33) android.Manifest.permission.READ_EXTERNAL_STORAGE else android.Manifest.permission.READ_MEDIA_IMAGES)) }

        var shouldLaunchCamera by remember { mutableStateOf(false) }
        var shouldLaunchGallery by remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            isGranted.forEach { permission, granted ->
                when (permission) {
                    android.Manifest.permission.CAMERA -> {
                        cameraPermission = granted
                        if (granted && shouldLaunchCamera) {
                            cameraUri?.let { takePictureLauncher.launch(it) }
                        }
                    }
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_MEDIA_IMAGES -> {
                        galleryPermission = granted
                        if (granted && shouldLaunchGallery) {
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    }
                }
            }
            shouldLaunchCamera = false
            shouldLaunchGallery = false
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.Bottom
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                reverseLayout = true
            ) {
                itemsIndexed(chatState.chatList) { _, chat ->
                    if (chat.isFromUser) {
                        UserChatItem(prompt = chat.prompt, bitmap = chat.bitmap)
                    } else {
                        ModelChatItem(response = chat.prompt)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    bitmap?.let {
                        Image(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(bottom = 2.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentDescription = "picked image",
                            contentScale = ContentScale.Crop,
                            bitmap = it.asImageBitmap()
                        )
                    }

                    Row {
                        // Button to pick an image from gallery
                        Icon(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    if (galleryPermission) {
                                        imagePicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    } else {
                                        shouldLaunchGallery = true
                                        permissionLauncher.launch(
                                            arrayOf(
                                                if (Build.VERSION.SDK_INT < 33)
                                                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                                                else
                                                    android.Manifest.permission.READ_MEDIA_IMAGES
                                            )
                                        )
                                    }
                                },
                            imageVector = Icons.Rounded.AddPhotoAlternate,
                            contentDescription = "Pick Photo",
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Button to capture an image with camera
                        Icon(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    if (cameraPermission) {
                                        cameraUri?.let {
                                            takePictureLauncher.launch(it)
                                        }
                                    } else {
                                        shouldLaunchCamera = true
                                        permissionLauncher.launch(arrayOf(
                                            android.Manifest.permission.CAMERA
                                        ))
                                    }
                                },
                            imageVector = Icons.Rounded.AddAPhoto,
                            contentDescription = "Capture Photo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            chaViewModel.onEvent(ChatUiEvent.SendImage(bitmap))
                            uriState.update { "" }
                        },
                    imageVector = Icons.Rounded.Send,
                    contentDescription = "Send image",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }


    @Composable
    fun UserChatItem(prompt: String, bitmap: Bitmap?) {
        Column(
            modifier = Modifier.padding(start = 100.dp, bottom = 16.dp)
        ) {
            bitmap?.let {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(bottom = 2.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentDescription = "image",
                    contentScale = ContentScale.Crop,
                    bitmap = it.asImageBitmap()
                )
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                text = prompt,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    @Composable
    fun ModelChatItem(response: String) {
        Column(
            modifier = Modifier.padding(end = 100.dp, bottom = 16.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Green)
                    .padding(16.dp),
                text = response,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    @Composable
    private fun getBitmap(): Bitmap? {
        val uri = uriState.collectAsState().value

        val imageState: AsyncImagePainter.State = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .size(Size.ORIGINAL)
                .build()
        ).state

        if (imageState is AsyncImagePainter.State.Success) {
            return imageState.result.drawable.toBitmap()
        }

        return null
    }
}

fun isPermissionGranted(context: Context,permission:String):Boolean
{
    return ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED
}