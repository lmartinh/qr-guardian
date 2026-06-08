package com.lmartin.qrguardian.domain.metadata

enum class DownloadFileType {
    Pdf,
    Document,
    Spreadsheet,
    Presentation,
    Image,
    Audio,
    Video,
    Archive,
    AndroidApp,
    AppleDiskImage,
    WindowsExecutable,
    Script,
    Unknown,
}

fun DownloadFileType.displayName(): String = when (this) {
    DownloadFileType.Pdf -> "PDF"
    DownloadFileType.Document -> "Document"
    DownloadFileType.Spreadsheet -> "Spreadsheet"
    DownloadFileType.Presentation -> "Presentation"
    DownloadFileType.Image -> "Image"
    DownloadFileType.Audio -> "Audio"
    DownloadFileType.Video -> "Video"
    DownloadFileType.Archive -> "Archive"
    DownloadFileType.AndroidApp -> "Android app"
    DownloadFileType.AppleDiskImage -> "Apple disk image"
    DownloadFileType.WindowsExecutable -> "Windows executable"
    DownloadFileType.Script -> "Script"
    DownloadFileType.Unknown -> "Unknown"
}
