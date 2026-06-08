package com.lmartin.qrguardian.presentation.result

import androidx.compose.runtime.Composable
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import org.jetbrains.compose.resources.stringResource
import qrguardian.shared.generated.resources.Res
import qrguardian.shared.generated.resources.result_detail_action
import qrguardian.shared.generated.resources.result_detail_contact
import qrguardian.shared.generated.resources.result_detail_content
import qrguardian.shared.generated.resources.result_detail_destination
import qrguardian.shared.generated.resources.result_detail_download
import qrguardian.shared.generated.resources.result_detail_extra_field
import qrguardian.shared.generated.resources.result_detail_file_extension
import qrguardian.shared.generated.resources.result_detail_file_name
import qrguardian.shared.generated.resources.result_detail_file_type
import qrguardian.shared.generated.resources.result_detail_host
import qrguardian.shared.generated.resources.result_detail_location
import qrguardian.shared.generated.resources.result_detail_message
import qrguardian.shared.generated.resources.result_detail_network
import qrguardian.shared.generated.resources.result_detail_number
import qrguardian.shared.generated.resources.result_detail_path
import qrguardian.shared.generated.resources.result_detail_payment
import qrguardian.shared.generated.resources.result_detail_plain_text
import qrguardian.shared.generated.resources.result_detail_resolved_destination
import qrguardian.shared.generated.resources.result_detail_scheme
import qrguardian.shared.generated.resources.result_detail_security
import qrguardian.shared.generated.resources.result_detail_security_key
import qrguardian.shared.generated.resources.result_detail_ssid
import qrguardian.shared.generated.resources.result_detail_ssid_key
import qrguardian.shared.generated.resources.result_detail_state
import qrguardian.shared.generated.resources.result_detail_type
import qrguardian.shared.generated.resources.result_detail_unknown
import qrguardian.shared.generated.resources.result_detail_usage
import qrguardian.shared.generated.resources.result_download_app_file_caution
import qrguardian.shared.generated.resources.result_download_file
import qrguardian.shared.generated.resources.result_download_file_caution
import qrguardian.shared.generated.resources.result_error_title
import qrguardian.shared.generated.resources.result_file_type_apk
import qrguardian.shared.generated.resources.result_file_type_audio
import qrguardian.shared.generated.resources.result_file_type_document
import qrguardian.shared.generated.resources.result_file_type_image
import qrguardian.shared.generated.resources.result_file_type_pdf
import qrguardian.shared.generated.resources.result_file_type_presentation
import qrguardian.shared.generated.resources.result_file_type_rar
import qrguardian.shared.generated.resources.result_file_type_seven_zip
import qrguardian.shared.generated.resources.result_file_type_spreadsheet
import qrguardian.shared.generated.resources.result_file_type_video
import qrguardian.shared.generated.resources.result_file_type_zip
import qrguardian.shared.generated.resources.result_idle_message
import qrguardian.shared.generated.resources.result_idle_title
import qrguardian.shared.generated.resources.result_level_dangerous_description
import qrguardian.shared.generated.resources.result_level_dangerous_title
import qrguardian.shared.generated.resources.result_level_safe_description
import qrguardian.shared.generated.resources.result_level_safe_title
import qrguardian.shared.generated.resources.result_level_suspicious_description
import qrguardian.shared.generated.resources.result_level_suspicious_title
import qrguardian.shared.generated.resources.result_level_unknown_description
import qrguardian.shared.generated.resources.result_level_unknown_title
import qrguardian.shared.generated.resources.result_loading_action
import qrguardian.shared.generated.resources.result_loading_message
import qrguardian.shared.generated.resources.result_loading_title
import qrguardian.shared.generated.resources.result_local_scan
import qrguardian.shared.generated.resources.result_local_signals
import qrguardian.shared.generated.resources.result_open_document
import qrguardian.shared.generated.resources.result_open_file
import qrguardian.shared.generated.resources.result_open_image
import qrguardian.shared.generated.resources.result_open_link
import qrguardian.shared.generated.resources.result_open_media
import qrguardian.shared.generated.resources.result_qr_content
import qrguardian.shared.generated.resources.result_remote_reputation
import qrguardian.shared.generated.resources.result_remote_status_completed
import qrguardian.shared.generated.resources.result_remote_status_not_applicable
import qrguardian.shared.generated.resources.result_remote_status_not_configured
import qrguardian.shared.generated.resources.result_remote_status_short_not_applicable
import qrguardian.shared.generated.resources.result_remote_status_short_not_configured
import qrguardian.shared.generated.resources.result_remote_status_short_unavailable
import qrguardian.shared.generated.resources.result_remote_status_unavailable
import qrguardian.shared.generated.resources.result_rescan
import qrguardian.shared.generated.resources.result_status_blocked
import qrguardian.shared.generated.resources.result_status_careful
import qrguardian.shared.generated.resources.result_status_dangerous
import qrguardian.shared.generated.resources.result_status_recommended
import qrguardian.shared.generated.resources.result_status_review
import qrguardian.shared.generated.resources.result_status_safe
import qrguardian.shared.generated.resources.result_status_uncertain
import qrguardian.shared.generated.resources.result_status_unknown
import qrguardian.shared.generated.resources.result_title
import qrguardian.shared.generated.resources.result_url_label
import qrguardian.shared.generated.resources.result_value_archive
import qrguardian.shared.generated.resources.result_value_crypto_action
import qrguardian.shared.generated.resources.result_value_crypto_type
import qrguardian.shared.generated.resources.result_value_downloadable_file
import qrguardian.shared.generated.resources.result_value_email_action
import qrguardian.shared.generated.resources.result_value_file
import qrguardian.shared.generated.resources.result_value_import_contact_action
import qrguardian.shared.generated.resources.result_value_insecure_http
import qrguardian.shared.generated.resources.result_value_location_type
import qrguardian.shared.generated.resources.result_value_no_predefined_message
import qrguardian.shared.generated.resources.result_value_no_subject_or_body
import qrguardian.shared.generated.resources.result_value_not_a_link
import qrguardian.shared.generated.resources.result_value_not_classified_precisely
import qrguardian.shared.generated.resources.result_value_not_specified
import qrguardian.shared.generated.resources.result_value_open_map_action
import qrguardian.shared.generated.resources.result_value_open_network
import qrguardian.shared.generated.resources.result_value_parameters_present
import qrguardian.shared.generated.resources.result_value_pdf_document
import qrguardian.shared.generated.resources.result_value_phone_action
import qrguardian.shared.generated.resources.result_value_phone_type
import qrguardian.shared.generated.resources.result_value_plain_text_type
import qrguardian.shared.generated.resources.result_value_predefined_fields
import qrguardian.shared.generated.resources.result_value_present
import qrguardian.shared.generated.resources.result_value_redirected_to
import qrguardian.shared.generated.resources.result_value_secure_https
import qrguardian.shared.generated.resources.result_value_server_suggests_file_download
import qrguardian.shared.generated.resources.result_value_sms_action
import qrguardian.shared.generated.resources.result_value_sms_type
import qrguardian.shared.generated.resources.result_value_unknown_binary_file
import qrguardian.shared.generated.resources.result_value_unknown_type
import qrguardian.shared.generated.resources.result_value_vcard_type
import qrguardian.shared.generated.resources.result_value_web_page
import qrguardian.shared.generated.resources.result_value_wifi_action
import qrguardian.shared.generated.resources.result_value_wifi_type

@Composable
internal fun rememberResultTexts(): ResultTexts = ResultTexts(
    title = stringResource(Res.string.result_title),
    openDocument = stringResource(Res.string.result_open_document),
    openImage = stringResource(Res.string.result_open_image),
    openMedia = stringResource(Res.string.result_open_media),
    openLink = stringResource(Res.string.result_open_link),
    openFile = stringResource(Res.string.result_open_file),
    downloadFile = stringResource(Res.string.result_download_file),
    downloadFileCaution = stringResource(Res.string.result_download_file_caution),
    downloadAppFileCaution = stringResource(Res.string.result_download_app_file_caution),
    rescan = stringResource(Res.string.result_rescan),
    localScan = stringResource(Res.string.result_local_scan),
    localSignals = stringResource(Res.string.result_local_signals),
    remoteReputation = stringResource(Res.string.result_remote_reputation),
    statusRecommended = stringResource(Res.string.result_status_recommended),
    statusReview = stringResource(Res.string.result_status_review),
    statusBlocked = stringResource(Res.string.result_status_blocked),
    statusUncertain = stringResource(Res.string.result_status_uncertain),
    statusSafe = stringResource(Res.string.result_status_safe),
    statusCareful = stringResource(Res.string.result_status_careful),
    statusDangerous = stringResource(Res.string.result_status_dangerous),
    statusUnknown = stringResource(Res.string.result_status_unknown),
    levelSafeTitle = stringResource(Res.string.result_level_safe_title),
    levelSafeDescription = stringResource(Res.string.result_level_safe_description),
    levelSuspiciousTitle = stringResource(Res.string.result_level_suspicious_title),
    levelSuspiciousDescription = stringResource(Res.string.result_level_suspicious_description),
    levelDangerousTitle = stringResource(Res.string.result_level_dangerous_title),
    levelDangerousDescription = stringResource(Res.string.result_level_dangerous_description),
    levelUnknownTitle = stringResource(Res.string.result_level_unknown_title),
    levelUnknownDescription = stringResource(Res.string.result_level_unknown_description),
    qrContentLabel = stringResource(Res.string.result_qr_content),
    urlLabel = stringResource(Res.string.result_url_label),
    errorTitle = stringResource(Res.string.result_error_title),
    idleTitle = stringResource(Res.string.result_idle_title),
    idleMessage = stringResource(Res.string.result_idle_message),
    loadingTitle = stringResource(Res.string.result_loading_title),
    loadingMessage = stringResource(Res.string.result_loading_message),
    loadingAction = stringResource(Res.string.result_loading_action),
    detailType = stringResource(Res.string.result_detail_type),
    detailAction = stringResource(Res.string.result_detail_action),
    detailHost = stringResource(Res.string.result_detail_host),
    detailScheme = stringResource(Res.string.result_detail_scheme),
    detailContent = stringResource(Res.string.result_detail_content),
    detailDestination = stringResource(Res.string.result_detail_destination),
    detailResolvedDestination = stringResource(Res.string.result_detail_resolved_destination),
    detailExtraField = stringResource(Res.string.result_detail_extra_field),
    detailNumber = stringResource(Res.string.result_detail_number),
    detailMessage = stringResource(Res.string.result_detail_message),
    detailSecurity = stringResource(Res.string.result_detail_security),
    detailNetwork = stringResource(Res.string.result_detail_network),
    detailSsid = stringResource(Res.string.result_detail_ssid),
    detailSsidKey = stringResource(Res.string.result_detail_ssid_key),
    detailSecurityKey = stringResource(Res.string.result_detail_security_key),
    detailContact = stringResource(Res.string.result_detail_contact),
    detailLocation = stringResource(Res.string.result_detail_location),
    detailPayment = stringResource(Res.string.result_detail_payment),
    detailPlainText = stringResource(Res.string.result_detail_plain_text),
    detailUnknown = stringResource(Res.string.result_detail_unknown),
    detailUsage = stringResource(Res.string.result_detail_usage),
    detailState = stringResource(Res.string.result_detail_state),
    detailFileName = stringResource(Res.string.result_detail_file_name),
    detailFileExtension = stringResource(Res.string.result_detail_file_extension),
    detailFileType = stringResource(Res.string.result_detail_file_type),
    detailPath = stringResource(Res.string.result_detail_path),
    detailDownload = stringResource(Res.string.result_detail_download),
    noValue = stringResource(Res.string.result_value_not_specified),
    noSubjectOrBody = stringResource(Res.string.result_value_no_subject_or_body),
    predefinedFields = stringResource(Res.string.result_value_predefined_fields),
    parametersPresent = stringResource(Res.string.result_value_parameters_present),
    noPredefinedMessage = stringResource(Res.string.result_value_no_predefined_message),
    notClassifiedPrecisely = stringResource(Res.string.result_value_not_classified_precisely),
    notALink = stringResource(Res.string.result_value_not_a_link),
    emailAction = stringResource(Res.string.result_value_email_action),
    phoneType = stringResource(Res.string.result_value_phone_type),
    phoneAction = stringResource(Res.string.result_value_phone_action),
    smsType = stringResource(Res.string.result_value_sms_type),
    smsAction = stringResource(Res.string.result_value_sms_action),
    wifiType = stringResource(Res.string.result_value_wifi_type),
    wifiAction = stringResource(Res.string.result_value_wifi_action),
    vCardType = stringResource(Res.string.result_value_vcard_type),
    importContactAction = stringResource(Res.string.result_value_import_contact_action),
    locationType = stringResource(Res.string.result_value_location_type),
    openMapAction = stringResource(Res.string.result_value_open_map_action),
    cryptoType = stringResource(Res.string.result_value_crypto_type),
    cryptoAction = stringResource(Res.string.result_value_crypto_action),
    plainTextType = stringResource(Res.string.result_value_plain_text_type),
    unknownType = stringResource(Res.string.result_value_unknown_type),
    webPageType = stringResource(Res.string.result_value_web_page),
    secureHttpsConnection = stringResource(Res.string.result_value_secure_https),
    insecureHttpConnection = stringResource(Res.string.result_value_insecure_http),
    pdfDocumentType = stringResource(Res.string.result_value_pdf_document),
    archiveType = stringResource(Res.string.result_value_archive),
    fileType = stringResource(Res.string.result_value_file),
    unknownBinaryFileType = stringResource(Res.string.result_value_unknown_binary_file),
    downloadableFile = stringResource(Res.string.result_value_downloadable_file),
    serverSuggestsFileDownload = stringResource(Res.string.result_value_server_suggests_file_download),
    redirectedTo = stringResource(Res.string.result_value_redirected_to),
    openNetwork = stringResource(Res.string.result_value_open_network),
    present = stringResource(Res.string.result_value_present),
    fileTypeApk = stringResource(Res.string.result_file_type_apk),
    fileTypePdf = stringResource(Res.string.result_file_type_pdf),
    fileTypeZip = stringResource(Res.string.result_file_type_zip),
    fileTypeRar = stringResource(Res.string.result_file_type_rar),
    fileTypeSevenZip = stringResource(Res.string.result_file_type_seven_zip),
    fileTypeDocument = stringResource(Res.string.result_file_type_document),
    fileTypeSpreadsheet = stringResource(Res.string.result_file_type_spreadsheet),
    fileTypePresentation = stringResource(Res.string.result_file_type_presentation),
    fileTypeAudio = stringResource(Res.string.result_file_type_audio),
    fileTypeVideo = stringResource(Res.string.result_file_type_video),
    fileTypeImage = stringResource(Res.string.result_file_type_image),
    remoteCompleted = stringResource(Res.string.result_remote_status_completed),
    remoteNotConfigured = stringResource(Res.string.result_remote_status_not_configured),
    remoteNotApplicable = stringResource(Res.string.result_remote_status_not_applicable),
    remoteUnavailable = stringResource(Res.string.result_remote_status_unavailable),
    remoteShortNotConfigured = stringResource(Res.string.result_remote_status_short_not_configured),
    remoteShortNotApplicable = stringResource(Res.string.result_remote_status_short_not_applicable),
    remoteShortUnavailable = stringResource(Res.string.result_remote_status_short_unavailable),
)

internal data class ResultTexts(
    val title: String,
    val openDocument: String,
    val openImage: String,
    val openMedia: String,
    val openLink: String,
    val openFile: String,
    val downloadFile: String,
    val downloadFileCaution: String,
    val downloadAppFileCaution: String,
    val rescan: String,
    val localScan: String,
    val localSignals: String,
    val remoteReputation: String,
    val statusRecommended: String,
    val statusReview: String,
    val statusBlocked: String,
    val statusUncertain: String,
    val statusSafe: String,
    val statusCareful: String,
    val statusDangerous: String,
    val statusUnknown: String,
    val levelSafeTitle: String,
    val levelSafeDescription: String,
    val levelSuspiciousTitle: String,
    val levelSuspiciousDescription: String,
    val levelDangerousTitle: String,
    val levelDangerousDescription: String,
    val levelUnknownTitle: String,
    val levelUnknownDescription: String,
    val qrContentLabel: String,
    val urlLabel: String,
    val errorTitle: String,
    val idleTitle: String,
    val idleMessage: String,
    val loadingTitle: String,
    val loadingMessage: String,
    val loadingAction: String,
    val detailType: String,
    val detailAction: String,
    val detailHost: String,
    val detailScheme: String,
    val detailContent: String,
    val detailDestination: String,
    val detailResolvedDestination: String,
    val detailExtraField: String,
    val detailNumber: String,
    val detailMessage: String,
    val detailSecurity: String,
    val detailNetwork: String,
    val detailSsid: String,
    val detailSsidKey: String,
    val detailSecurityKey: String,
    val detailContact: String,
    val detailLocation: String,
    val detailPayment: String,
    val detailPlainText: String,
    val detailUnknown: String,
    val detailUsage: String,
    val detailState: String,
    val detailFileName: String,
    val detailFileExtension: String,
    val detailFileType: String,
    val detailPath: String,
    val detailDownload: String,
    val noValue: String,
    val noSubjectOrBody: String,
    val predefinedFields: String,
    val parametersPresent: String,
    val noPredefinedMessage: String,
    val notClassifiedPrecisely: String,
    val notALink: String,
    val emailAction: String,
    val phoneType: String,
    val phoneAction: String,
    val smsType: String,
    val smsAction: String,
    val wifiType: String,
    val wifiAction: String,
    val vCardType: String,
    val importContactAction: String,
    val locationType: String,
    val openMapAction: String,
    val cryptoType: String,
    val cryptoAction: String,
    val plainTextType: String,
    val unknownType: String,
    val webPageType: String,
    val secureHttpsConnection: String,
    val insecureHttpConnection: String,
    val pdfDocumentType: String,
    val archiveType: String,
    val fileType: String,
    val unknownBinaryFileType: String,
    val downloadableFile: String,
    val serverSuggestsFileDownload: String,
    val redirectedTo: String,
    val openNetwork: String,
    val present: String,
    val fileTypeApk: String,
    val fileTypePdf: String,
    val fileTypeZip: String,
    val fileTypeRar: String,
    val fileTypeSevenZip: String,
    val fileTypeDocument: String,
    val fileTypeSpreadsheet: String,
    val fileTypePresentation: String,
    val fileTypeAudio: String,
    val fileTypeVideo: String,
    val fileTypeImage: String,
    val remoteCompleted: String,
    val remoteNotConfigured: String,
    val remoteNotApplicable: String,
    val remoteUnavailable: String,
    val remoteShortNotConfigured: String,
    val remoteShortNotApplicable: String,
    val remoteShortUnavailable: String,
)

internal fun ResultTexts.levelTitle(level: SecurityLevel): String = when (level) {
    SecurityLevel.Safe -> levelSafeTitle
    SecurityLevel.Suspicious -> levelSuspiciousTitle
    SecurityLevel.Dangerous -> levelDangerousTitle
    SecurityLevel.Unknown -> levelUnknownTitle
}

internal fun ResultTexts.levelDescription(level: SecurityLevel): String = when (level) {
    SecurityLevel.Safe -> levelSafeDescription
    SecurityLevel.Suspicious -> levelSuspiciousDescription
    SecurityLevel.Dangerous -> levelDangerousDescription
    SecurityLevel.Unknown -> levelUnknownDescription
}

internal fun ResultTexts.sectionSummary(section: ScanSectionResult): String = when (section.status) {
    ScanStatus.Completed -> levelDescription(section.level)
    ScanStatus.NotConfigured -> remoteNotConfigured
    ScanStatus.NotApplicable -> remoteNotApplicable
    ScanStatus.Unavailable -> remoteUnavailable
}
