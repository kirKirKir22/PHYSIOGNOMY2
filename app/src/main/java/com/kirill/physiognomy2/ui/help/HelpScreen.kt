package com.kirill.physiognomy2.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HelpScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "ИНСТРУКЦИЯ",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = "Как читать лицо",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Короткий порядок работы в приложении",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(18.dp))
        HelpStep(
            number = "1",
            icon = Icons.Filled.AddPhotoAlternate,
            title = "Добавьте фото",
            text = "Загрузите до 10 снимков одного лица в разных ракурсах (анфас, профиль, три четверти)." +
                    " Свайп переключает фото, щипок масштабирует, двойной тап приближает. " +
                    "Пунктирная линия по центру помогает выровнять лицо.",
        )
        HelpStep(
            number = "2",
            icon = Icons.Filled.BrightnessMedium,
            title = "Подготовьте снимок",
            text = "Яркость и контраст, ч/б и зеркало помогают разглядеть детали." +
                    " Кнопка «Эффекты» мгновенно возвращает оригинал для сравнения.",
        )
        HelpStep(
            number = "3",
            icon = Icons.AutoMirrored.Filled.Assignment,
            title = "Отмечайте черты по шагам",
            text = "В каждом шаге выберите вариант: лоб, брови, глаза, нос и так далее — всего 26 шагов. " +
                    "Отмеченная карточка сразу показывает значение черты. Повторное нажатие снимает выбор.",
        )
        HelpStep(
            number = "4",
            icon = Icons.Filled.SkipNext,
            title = "Можно пропускать",
            text = "Если черта не читается или сомнительна, нажмите «Пропустить». " +
                    "Приложение не требует заполнять всё: вывод строится только из отмеченных черт," +
                    " а пропущенные собираются в отдельный список.",
        )
        HelpStep(
            number = "5",
            icon = Icons.Filled.ContentCopy,
            title = "Получите вывод",
            text = "На последнем шаге откройте «Итог»: там собрано описание по всем отмеченным чертам. " +
                    "Текст можно скопировать, отправить в мессенджер или сохранить в профили.",
        )

        Spacer(Modifier.height(18.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Как смотреть",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Сравнивайте черту с обеих сторон лица: левая половина связана с личной жизнью, правая — с деловой. Читайте только заметные отклонения, а не мелкие различия. Значения черт — это язык наблюдений, а не приговор.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = "Материалы основаны на книге Мэк Фулфер «Искусство чтения по лицу» (иллюстрированная энциклопедия физиогномики).",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun HelpStep(
    number: String,
    icon: ImageVector,
    title: String,
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun HelpBadge(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
            )
            Text("Инструкция", style = MaterialTheme.typography.labelSmall)
        }
    }
}
