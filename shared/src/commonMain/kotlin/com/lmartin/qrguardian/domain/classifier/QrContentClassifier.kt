package com.lmartin.qrguardian.domain.classifier

import com.lmartin.qrguardian.domain.model.QrContentType

interface QrContentClassifier {
    fun classify(text: String): QrContentType
}
