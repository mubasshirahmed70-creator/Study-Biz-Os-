package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.model.BusinessProductEntity
import com.example.ui.theme.BusinessAccent
import com.example.ui.theme.PriorityWarning
import com.example.ui.theme.StudyPrimary
import com.example.ui.viewmodel.StudyBizViewModel
import java.util.Locale

data class DropshipWorkflowStage(
    val name: String,
    val icon: ImageVector,
    val description: String,
    val tips: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessScreen(
    viewModel: StudyBizViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Product Database, 1: Dropship Workflows
    var showAddProductDialog by remember { mutableStateOf(false) }
    var selectedWorkflowStage by remember { mutableStateOf<DropshipWorkflowStage?>(null) }

    val workflowStages = listOf(
        DropshipWorkflowStage("প্রোডাক্ট রিসার্চ", Icons.Default.Search, "AliExpress উইনিং ক্রাইটেরিয়া", "৫০০+ অর্ডার, ৪.৭+ রেটিং, নির্দিষ্ট সমস্যার সমাধান এবং ১৫ ডলারের বেশি মার্জিন খুঁজুন।"),
        DropshipWorkflowStage("সাপ্লায়ার যাচাই", Icons.Default.LocalShipping, "AliExpress ও ড্রপশিপিং এজেন্ট", "শিপিং টাইম ও বিক্রেতার রেটিং যাচাই করুন।"),
        DropshipWorkflowStage("স্টোর সেটআপ", Icons.Default.Storefront, "Shopify থিম ও পলিসি", "সহজ ও পরিচ্ছন্ন ডিজাইন, রিটার্ন পলিসি এবং ট্রাস্ট ব্যাজ রাখুন।"),
        DropshipWorkflowStage("প্রোডাক্ট লিস্টিং", Icons.Default.Inventory, "বিবরণ ও আকর্ষণীয় ছবি", "ফিচারের চেয়ে কাস্টমারের ৩টি মূল উপকার তুলে ধরুন।"),
        DropshipWorkflowStage("বিজ্ঞাপনের ভিডিও/ছবি", Icons.Default.Palette, "Canva ও CapCut ক্রিয়েটিভ", "ভিডিওর প্রথম ৩ সেকেন্ডে আকর্ষণীয় হুক ব্যবহার করুন।"),
        DropshipWorkflowStage("ফেসবুক অ্যাডস", Icons.Default.Campaign, "টার্গেটিং ও ব্রড অডিয়েন্স", "কম বাজেটে টেস্ট করুন। মেটা অ্যাডস ম্যানেজারের অ্যানালিটিক্স শিখুন।"),
        DropshipWorkflowStage("অর্গানিক রিলস", Icons.Default.Campaign, "TikTok ও Instagram শর্টস", "ট্রেন্ডিং সাউন্ড দিয়ে প্রোডাক্টের ব্যবহারের রিল পোস্ট করুন।"),
        DropshipWorkflowStage("অ্যানালিটিক্স পর্যালোচনা", Icons.Default.Analytics, "ROAS ও কনভার্সন রেট", "স্টোর সেশন ও কার্ট অ্যাড রেট বিশ্লেষণ করুন।"),
        DropshipWorkflowStage("কাস্টমার সাপোর্ট", Icons.Default.SupportAgent, "ইমেইল ও মেসেজ টেমপ্লেট", "২৪ ঘণ্টার মধ্যে কাস্টমারের ট্র্যাকিং প্রশ্নের বিনীত উত্তর দিন।"),
        DropshipWorkflowStage("অর্ডার ব্যবস্থাপনা", Icons.Default.CheckCircle, "ফুলফিলমেন্ট রিভিউ", "অর্ডার পাঠানোর আগে কাস্টমারের ঠিকানা ম্যানুয়ালি নিশ্চিত করুন।")
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "ড্রপশিপিং ব্যবসা মডিউল",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BusinessAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "শপিফাই ও আলিয়েক্সপ্রেস হাব",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "দ্বিতীয় অগ্রাধিকার • দৈনিক সর্বোচ্চ সীমা: ${settings.dailyBusinessHourCap} ঘণ্টা",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Mindset & Safety Disclaimer Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dropshipping_mindset_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BusinessAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = BusinessAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "শেখার স্যান্ডবক্স ও ব্যবসায়িক মানসিকতা",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ড্রপশিপিং হলো মার্কেটিং ও ডিজিটাল স্কিল অর্জনের একটি প্র্যাকটিক্যাল লার্নিং প্ল্যাটফর্ম। এসএসসি পড়াশোনা ও ভালো রেজাল্ট আপনার আসল ভিত্তি।",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Tab Selector
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("প্রোডাক্ট রিসার্চ (${products.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("ড্রপশিপিং ওয়ার্কফ্লো") }
                    )
                }
            }

            if (selectedTab == 0) {
                // Product Database
                if (products.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No products researched yet.", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Add items from AliExpress to evaluate margins and viability.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onUpdateStatus = { newStatus -> viewModel.updateProductStatus(product, newStatus) },
                            onDelete = { viewModel.deleteProduct(product) }
                        )
                    }
                }
            } else {
                // Dropship Workflows List
                items(workflowStages) { stage ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedWorkflowStage = stage }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BusinessAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(stage.icon, contentDescription = null, tint = BusinessAccent, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stage.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(stage.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // FAB to add product
        if (selectedTab == 0) {
            FloatingActionButton(
                onClick = { showAddProductDialog = true },
                containerColor = BusinessAccent,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 20.dp)
                    .testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    }

    // Dialog: Add Product
    if (showAddProductDialog) {
        var name by remember { mutableStateOf("") }
        var supplier by remember { mutableStateOf("AliExpress") }
        var url by remember { mutableStateOf("") }
        var costStr by remember { mutableStateOf("") }
        var priceStr by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Gadgets") }
        var notes by remember { mutableStateOf("") }

        val cost = costStr.toDoubleOrNull() ?: 0.0
        val price = priceStr.toDoubleOrNull() ?: 0.0
        val margin = (price - cost).coerceAtLeast(0.0)

        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Add Researched Product") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                    )
                    OutlinedTextField(
                        value = supplier,
                        onValueChange = { supplier = it },
                        label = { Text("Supplier (e.g. AliExpress)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = costStr,
                            onValueChange = { costStr = it },
                            label = { Text("Cost ($)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Selling Price ($)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (price > 0 && cost > 0) {
                        Surface(
                            color = BusinessAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Estimated Profit Margin: $${String.format(Locale.getDefault(), "%.2f", margin)} (${if (price > 0) (margin / price * 100).toInt() else 0}%)",
                                color = BusinessAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Marketing Angle") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addProduct(
                                name = name.trim(),
                                supplierName = supplier.trim(),
                                supplierUrl = url.trim(),
                                costPrice = cost,
                                sellingPrice = price,
                                status = "RESEARCHING",
                                category = category,
                                notes = notes.trim()
                            )
                            showAddProductDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_product")
                ) {
                    Text("Save Product")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Workflow Tips
    selectedWorkflowStage?.let { stage ->
        AlertDialog(
            onDismissRequest = { selectedWorkflowStage = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(stage.icon, contentDescription = null, tint = BusinessAccent, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stage.name)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stage.description, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Best Practice Checklist:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BusinessAccent)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(stage.tips, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedWorkflowStage = null }) { Text("Got It") }
            }
        )
    }
}

@Composable
fun ProductCard(
    product: BusinessProductEntity,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Supplier: ${product.supplierName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pricing Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Cost", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${String.format(Locale.getDefault(), "%.2f", product.costPrice)}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Price", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${String.format(Locale.getDefault(), "%.2f", product.sellingPrice)}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    color = BusinessAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Est. Margin", fontSize = 10.sp, color = BusinessAccent)
                        Text("+$${String.format(Locale.getDefault(), "%.2f", product.estimatedMargin)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BusinessAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Selector Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statuses = listOf("RESEARCHING", "TESTING", "READY_TO_LAUNCH", "WINNER", "DROPPED")
                items(statuses) { status ->
                    FilterChip(
                        selected = product.status == status,
                        onClick = { onUpdateStatus(status) },
                        label = { Text(status.replace("_", " "), fontSize = 10.sp) }
                    )
                }
            }

            if (product.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notes: ${product.notes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
