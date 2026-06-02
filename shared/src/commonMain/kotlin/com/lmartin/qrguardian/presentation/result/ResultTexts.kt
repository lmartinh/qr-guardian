package com.lmartin.qrguardian.presentation.result

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import qrguardian.shared.generated.resources.Res
import qrguardian.shared.generated.resources.result_detail_action
import qrguardian.shared.generated.resources.result_detail_contact
import qrguardian.shared.generated.resources.result_detail_destination
import qrguardian.shared.generated.resources.result_detail_extra_field
import qrguardian.shared.generated.resources.result_detail_file
import qrguardian.shared.generated.resources.result_detail_location
import qrguardian.shared.generated.resources.result_detail_message
import qrguardian.shared.generated.resources.result_detail_network
import qrguardian.shared.generated.resources.result_detail_number
import qrguardian.shared.generated.resources.result_detail_path
import qrguardian.shared.generated.resources.result_detail_payment
import qrguardian.shared.generated.resources.result_detail_plain_text
import qrguardian.shared.generated.resources.result_detail_security
import qrguardian.shared.generated.resources.result_detail_security_key
import qrguardian.shared.generated.resources.result_detail_ssid
import qrguardian.shared.generated.resources.result_detail_ssid_key
import qrguardian.shared.generated.resources.result_detail_state
import qrguardian.shared.generated.resources.result_detail_type
import qrguardian.shared.generated.resources.result_detail_unknown
import qrguardian.shared.generated.resources.result_detail_usage
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
import qrguardian.shared.generated.resources.result_loading_action
import qrguardian.shared.generated.resources.result_loading_message
import qrguardian.shared.generated.resources.result_loading_title
import qrguardian.shared.generated.resources.result_local_scan
import qrguardian.shared.generated.resources.result_local_signals
import qrguardian.shared.generated.resources.result_open_link
import qrguardian.shared.generated.resources.result_remote_checks
import qrguardian.shared.generated.resources.result_remote_not_applicable
import qrguardian.shared.generated.resources.result_remote_not_configured
import qrguardian.shared.generated.resources.result_remote_reputation
import qrguardian.shared.generated.resources.result_remote_status_completed
import qrguardian.shared.generated.resources.result_remote_status_not_applicable
import qrguardian.shared.generated.resources.result_remote_status_not_configured
import qrguardian.shared.generated.resources.result_remote_status_unavailable
import qrguardian.shared.generated.resources.result_remote_unavailable
import qrguardian.shared.generated.resources.result_rescan
import qrguardian.shared.generated.resources.result_status_blocked
import qrguardian.shared.generated.resources.result_status_recommended
import qrguardian.shared.generated.resources.result_status_review
import qrguardian.shared.generated.resources.result_status_uncertain
import qrguardian.shared.generated.resources.result_title
import qrguardian.shared.generated.resources.result_url_analyzed
import qrguardian.shared.generated.resources.result_url_label
import qrguardian.shared.generated.resources.result_value_crypto_action
import qrguardian.shared.generated.resources.result_value_crypto_type
import qrguardian.shared.generated.resources.result_value_email_action
import qrguardian.shared.generated.resources.result_value_import_contact_action
import qrguardian.shared.generated.resources.result_value_location_type
import qrguardian.shared.generated.resources.result_value_no_predefined_message
import qrguardian.shared.generated.resources.result_value_no_subject_or_body
import qrguardian.shared.generated.resources.result_value_not_a_link
import qrguardian.shared.generated.resources.result_value_not_classified_precisely
import qrguardian.shared.generated.resources.result_value_not_specified
import qrguardian.shared.generated.resources.result_value_open_map_action
import qrguardian.shared.generated.resources.result_value_parameters_present
import qrguardian.shared.generated.resources.result_value_phone_action
import qrguardian.shared.generated.resources.result_value_phone_type
import qrguardian.shared.generated.resources.result_value_plain_text_type
import qrguardian.shared.generated.resources.result_value_predefined_fields
import qrguardian.shared.generated.resources.result_value_sms_action
import qrguardian.shared.generated.resources.result_value_sms_type
import qrguardian.shared.generated.resources.result_value_unknown_type
import qrguardian.shared.generated.resources.result_value_vcard_type
import qrguardian.shared.generated.resources.result_value_wifi_action
import qrguardian.shared.generated.resources.result_value_wifi_type

@Composable
internal fun rememberResultTexts(): ResultTexts {
    return ResultTexts(
        title = stringResource(Res.string.result_title),
        openLink = stringResource(Res.string.result_open_link),
        rescan = stringResource(Res.string.result_rescan),
        localScan = stringResource(Res.string.result_local_scan),
        remoteReputation = stringResource(Res.string.result_remote_reputation),
        localSignals = stringResource(Res.string.result_local_signals),
        remoteChecks = stringResource(Res.string.result_remote_checks),
        statusRecommended = stringResource(Res.string.result_status_recommended),
        statusReview = stringResource(Res.string.result_status_review),
        statusBlocked = stringResource(Res.string.result_status_blocked),
        statusUncertain = stringResource(Res.string.result_status_uncertain),
        urlLabel = stringResource(Res.string.result_url_label),
        errorTitle = stringResource(Res.string.result_error_title),
        idleTitle = stringResource(Res.string.result_idle_title),
        idleMessage = stringResource(Res.string.result_idle_message),
        loadingTitle = stringResource(Res.string.result_loading_title),
        loadingMessage = stringResource(Res.string.result_loading_message),
        loadingAction = stringResource(Res.string.result_loading_action),
        remoteNotConfigured = stringResource(Res.string.result_remote_not_configured),
        remoteNotApplicable = stringResource(Res.string.result_remote_not_applicable),
        remoteUnavailable = stringResource(Res.string.result_remote_unavailable),
        detailType = stringResource(Res.string.result_detail_type),
        detailAction = stringResource(Res.string.result_detail_action),
        detailDestination = stringResource(Res.string.result_detail_destination),
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
        detailFile = stringResource(Res.string.result_detail_file),
        detailPath = stringResource(Res.string.result_detail_path),
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
        remoteNotConfiguredStatus = stringResource(Res.string.result_remote_status_not_configured),
        remoteNotApplicableStatus = stringResource(Res.string.result_remote_status_not_applicable),
        remoteUnavailableStatus = stringResource(Res.string.result_remote_status_unavailable),
    )
}

internal data class ResultTexts(
    val title: String,
    val openLink: String,
    val rescan: String,
    val localScan: String,
    val remoteReputation: String,
    val localSignals: String,
    val remoteChecks: String,
    val statusRecommended: String,
    val statusReview: String,
    val statusBlocked: String,
    val statusUncertain: String,
    val urlLabel: String,
    val errorTitle: String,
    val idleTitle: String,
    val idleMessage: String,
    val loadingTitle: String,
    val loadingMessage: String,
    val loadingAction: String,
    val remoteNotConfigured: String,
    val remoteNotApplicable: String,
    val remoteUnavailable: String,
    val detailType: String,
    val detailAction: String,
    val detailDestination: String,
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
    val detailFile: String,
    val detailPath: String,
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
    val remoteNotConfiguredStatus: String,
    val remoteNotApplicableStatus: String,
    val remoteUnavailableStatus: String,
)
