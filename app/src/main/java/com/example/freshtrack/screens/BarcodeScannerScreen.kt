package com.example.freshtrack.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.example.freshtrack.api.OpenFoodFactsApi
import com.example.freshtrack.api.ProductData
import com.example.freshtrack.ui.components.OverlayView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalGetImage::class)
@Composable
fun BarcodeScannerScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scannedCode = remember { mutableStateOf<String?>(null) }
    val scannedProduct = remember { mutableStateOf<ProductData?>(null) }
    var manualModeEnabled by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (!manualModeEnabled) {
            Text("Open Barcode Scanner", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                factory = { ctx ->
                    val frameLayout = FrameLayout(ctx)

                    val previewView = PreviewView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val overlayView = OverlayView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    frameLayout.addView(previewView)
                    frameLayout.addView(overlayView)

                    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            (ctx as Activity),
                            arrayOf(Manifest.permission.CAMERA),
                            0
                        )
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val scanner = BarcodeScanning.getClient(
                            BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                                .build()
                        )

                        val analysis = ImageAnalysis.Builder().build().also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )

                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            for (barcode in barcodes) {
                                                barcode.rawValue?.let { scannedValue ->
                                                    scannedCode.value = scannedValue
                                                    Log.d("SCANNER", "Scanned: $scannedValue")

                                                    coroutineScope.launch {
                                                        val product = OpenFoodFactsApi.fetchProductByBarcode(scannedValue)
                                                        product?.let {
                                                            saveProductToFirestore(it)
                                                            scannedProduct.value = it
                                                        } ?: Log.d("SCANNER", "Product not found")
                                                    }
                                                }
                                            }
                                        }
                                        .addOnFailureListener {
                                            Log.e("SCANNER", "Error: ${it.message}")
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                analysis
                            )
                        } catch (e: Exception) {
                            Log.e("SCANNER", "Camera binding failed: ${e.message}")
                        }

                    }, ContextCompat.getMainExecutor(ctx))

                    frameLayout
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Scanned Code: ${scannedCode.value ?: "Waiting..."}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            scannedProduct.value?.let { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Product: ${product.name}", style = MaterialTheme.typography.titleMedium)
                        Text("Brand: ${product.brand}")
                        Text("Allergens: ${product.allergens}")
                        Text("Barcode: ${product.barcode}")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { manualModeEnabled = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Item Manually")
            }

        } else {
            ManualAddItemForm(
                onSave = { name, brand, allergens ->
                    val product = ProductData(
                        name = name,
                        brand = brand,
                        allergens = allergens.joinToString(", "),
                        barcode = System.currentTimeMillis().toString(),
                        ingredients = "",
                        category = "",
                        quantity = ""
                    )
                    saveProductToFirestore(product)
                    Log.d("ManualEntry", "Manually saved product: $product")
                    manualModeEnabled = false
                },
                onCancel = {
                    manualModeEnabled = false
                }
            )
        }
    }
}

fun saveProductToFirestore(product: ProductData) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId == null) {
        Log.e("FIRESTORE", "User not logged in")
        return
    }

    val db = FirebaseFirestore.getInstance()
    db.collection("users")
        .document(userId)
        .collection("scannedProducts")
        .document(product.barcode)
        .set(product)
        .addOnSuccessListener {
            Log.d("FIRESTORE", "Product saved under user: ${product.name}")
        }
        .addOnFailureListener {
            Log.e("FIRESTORE", "Error saving product: ${it.message}")
        }
}



@Composable
fun ManualAddItemForm(
    onSave: (name: String, brand: String, allergens: List<String>) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var allergensText by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Add Item Manually", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = brand,
            onValueChange = { brand = it },
            label = { Text("Brand") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = allergensText,
            onValueChange = { allergensText = it },
            label = { Text("Allergens (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                val allergensList = allergensText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                onSave(name, brand, allergensList)
            }) {
                Text("Save")
            }

            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}