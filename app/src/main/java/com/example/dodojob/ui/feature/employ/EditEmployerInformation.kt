@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.dodojob.ui.feature.employ

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dodojob.R
import com.example.dodojob.navigation.Route
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.ExperimentalLayoutApi

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 1.dp,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 6.dp,
    cornerRadius: Dp = 10.dp
) = this.drawBehind {
    val strokePx = strokeWidth.toPx()
    val dash = PathEffect.dashPathEffect(floatArrayOf(dashLength.toPx(), gapLength.toPx()), 0f)
    drawRoundRect(
        color = color,
        style = Stroke(width = strokePx, pathEffect = dash),
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    )
}

private val BrandBlue  = Color(0xFF005FFF)
private val TextGray   = Color(0xFF828282)
private val LineGray   = Color(0xFFDDDDDD)
private val BgGray     = Color(0xFFF1F5F7)

private val PretendardSemi = FontFamily(Font(R.font.pretendard_semibold, FontWeight.SemiBold))
private val PretendardMed  = FontFamily(Font(R.font.pretendard_medium,  FontWeight.Medium))

@Composable
private fun ScrollHeader(title: String, onBack: () -> Unit) {
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .height(72.dp)
                .padding(horizontal = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp)
                    .clickable { onBack() }
            )
            Text(
                text = title,
                fontSize = 24.sp,
                fontFamily = PretendardSemi,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    topPadding: Dp = 10.dp
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = 10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontFamily = PretendardSemi,
            color = Color.Black
        )
        Spacer(Modifier.height(10.dp))
        androidx.compose.material3.OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 43.dp),
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 15.sp,
                    fontFamily = PretendardMed,
                    color = TextGray
                )
            },
            textStyle = TextStyle(
                fontSize = 15.sp,
                fontFamily = PretendardMed,
                color = Color.Black
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TextGray,
                unfocusedBorderColor = TextGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
private fun LabeledSelectField(
    label: String,
    selected: String,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontFamily = PretendardSemi,
            color = Color.Black
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, TextGray, RoundedCornerShape(10.dp))
                .clickable { onClick() }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selected,
                    fontSize = 15.sp,
                    fontFamily = PretendardMed,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(id = R.drawable.down),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun LabeledMultilineField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    topPadding: Dp = 10.dp
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontFamily = PretendardSemi,
            color = Color.Black
        )
        Spacer(Modifier.height(10.dp))
        androidx.compose.material3.OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 13.sp,
                    fontFamily = PretendardMed,
                    color = TextGray,
                    lineHeight = 20.sp
                )
            },
            textStyle = TextStyle(
                fontSize = 13.sp,
                fontFamily = PretendardMed,
                color = Color.Black,
                lineHeight = 20.sp
            ),
            shape = RoundedCornerShape(10.dp),
            minLines = 4,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TextGray,
                unfocusedBorderColor = TextGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
private fun LogoUploadBox(onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .size(74.5.dp)
            .clip(shape)
            .background(Color.White)
            .dashedBorder(
                color = Color(0xFF68A0FE),
                strokeWidth = 0.5.dp,
                dashLength = 8.dp,
                gapLength = 6.dp,
                cornerRadius = 10.dp
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            "+",
            fontSize = 24.sp,
            color = Color(0xFF7EAAF3),
            fontFamily = PretendardSemi
        )
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(47.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontFamily = PretendardSemi,
            color = Color.White
        )
    }
}

@Composable
fun EditEmployerInformationScreen(navController: NavController) {
    val scroll = rememberScrollState()

    var companyName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("서비스업") }
    var homepage     by remember { mutableStateOf("") }
    var intro        by remember { mutableStateOf("") }
    var address      by remember { mutableStateOf("") }
    var addressDetail by remember { mutableStateOf("") }
    var managerName  by remember { mutableStateOf("") }
    var managerPhone by remember { mutableStateOf("") }
    var managerEmail by remember { mutableStateOf("") }
    var saveContact  by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgGray,
        contentWindowInsets = WindowInsets(0)
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .background(BgGray)
                .verticalScroll(scroll)
        ) {
            Spacer(
                modifier = Modifier
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(Color.White)
            )

            ScrollHeader(title = "기업 정보", onBack = { navController.popBackStack() })

            Box(
                Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LogoUploadBox { }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "회사 로고를 등록해주세요.\n권장크기 : 200x200px",
                            fontSize = 15.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center,
                            fontFamily = PretendardMed
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    LabeledTextField("회사명", companyName, { companyName = it }, "회사명을 적어주세요")
                    LabeledSelectField("업종", businessType) { }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "회사주소",
                        fontSize = 18.sp,
                        fontFamily = PretendardSemi,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(10.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 43.dp),
                        placeholder = {
                            Text(
                                "주소를 검색해주세요",
                                fontSize = 15.sp,
                                fontFamily = PretendardMed,
                                color = TextGray
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            fontFamily = PretendardMed,
                            color = Color.Black
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TextGray,
                            unfocusedBorderColor = TextGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text(
                            text = "주소찾기",
                            fontSize = 16.sp,
                            fontFamily = PretendardSemi,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    LabeledTextField("상세주소", addressDetail, { addressDetail = it }, "상세주소를 입력해주세요")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(BgGray)
                    .padding(top = 4.dp, bottom = 10.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    LabeledMultilineField(
                        label = "회사소개",
                        value = intro,
                        onValueChange = { intro = it },
                        placeholder = "예 : 풍부한 경험과 전문성을 갖춘 시니어 인재분들과 함께 성장하고 있습니다. 나이가 아닌 역량을 중심으로 평가하며, 시니어분들의 노하우를 젊은 세대와 공유하는 상생의 기업문화를 만들어가고 있습니다.",
                        topPadding = 0.dp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "회사 홈페이지",
                        fontSize = 18.sp,
                        fontFamily = PretendardSemi,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(10.dp))
                    androidx.compose.material3.OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 43.dp),
                        value = homepage,
                        onValueChange = { homepage = it },
                        placeholder = {
                            Text(
                                "https://www.sng.com",
                                fontSize = 15.sp,
                                fontFamily = PretendardMed,
                                color = TextGray,
                                textDecoration = TextDecoration.Underline
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            fontFamily = PretendardMed,
                            color = Color.Black,
                            textDecoration = TextDecoration.Underline
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TextGray,
                            unfocusedBorderColor = TextGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(BgGray)
                    .padding(top = 4.dp, bottom = 10.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    LabeledTextField(
                        label = "담당자명",
                        value = managerName,
                        onValueChange = { managerName = it },
                        placeholder = "담당자 성함",
                        topPadding = 0.dp
                    )
                    LabeledTextField(
                        "담당자 연락처",
                        managerPhone,
                        { managerPhone = it },
                        "010-0000-0000",
                        keyboardType = KeyboardType.Phone
                    )
                    LabeledTextField(
                        "담당자 이메일",
                        managerEmail,
                        { managerEmail = it },
                        "이메일을 입력해주세요",
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (saveContact)
                                    R.drawable.announce_checked_button
                                else
                                    R.drawable.announce_unchecked_button
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { saveContact = !saveContact }
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "입력한 담당자 정보 저장",
                            fontSize = 15.sp,
                            fontFamily = PretendardMed,
                            color = TextGray
                        )
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(BgGray)
                    .padding(top = 10.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    PrimaryButton(
                        text = "저장하기",
                        onClick = {
                            navController.navigate(Route.EmployerMy.path) {
                                launchSingleTop = true
                                popUpTo(Route.EmployerMy.path) { inclusive = false }
                            }
                        }
                    )
                }
            }
        }
    }
}
