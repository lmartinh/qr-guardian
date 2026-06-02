package com.lmartin.qrguardian.domain.classifier

import com.lmartin.qrguardian.domain.model.QrContentType
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultQrContentClassifierTest {
    private val classifier = DefaultQrContentClassifier()

    @Test
    fun `classifies url with https`() {
        assertEquals(QrContentType.Url, classifier.classify("https://example.com"))
    }

    @Test
    fun `classifies url with http`() {
        assertEquals(QrContentType.Url, classifier.classify("http://example.com"))
    }

    @Test
    fun `classifies mailto`() {
        assertEquals(QrContentType.Email, classifier.classify("mailto:test@example.com"))
    }

    @Test
    fun `classifies phone`() {
        assertEquals(QrContentType.Phone, classifier.classify("tel:+34600000000"))
    }

    @Test
    fun `classifies sms`() {
        assertEquals(QrContentType.Sms, classifier.classify("sms:+34600000000"))
    }

    @Test
    fun `classifies wifi`() {
        assertEquals(QrContentType.Wifi, classifier.classify("WIFI:T:WPA;S:Test;P:1234;;"))
    }

    @Test
    fun `classifies vcard`() {
        assertEquals(QrContentType.VCard, classifier.classify("BEGIN:VCARD"))
    }

    @Test
    fun `classifies geo`() {
        assertEquals(QrContentType.Geo, classifier.classify("geo:40.4168,-3.7038"))
    }

    @Test
    fun `classifies crypto`() {
        assertEquals(QrContentType.Crypto, classifier.classify("bitcoin:xxxx"))
    }

    @Test
    fun `classifies plain text`() {
        assertEquals(QrContentType.PlainText, classifier.classify("hello world"))
    }
}
