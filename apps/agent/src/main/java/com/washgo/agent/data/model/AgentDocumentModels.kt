package com.washgo.agent.data.model

enum class DocumentType {
    GOVERNMENT_ID,
    DRIVING_LICENSE,
    VEHICLE_REGISTRATION,
    POLICE_CLEARANCE,
    INSURANCE
}

enum class DocumentVerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED
}

data class AgentDocumentDto(
    val id: String,
    val agentId: String? = null,
    val documentType: String,
    val documentNumber: String? = null,
    val fileName: String? = null,
    val contentType: String? = null,
    val fileSize: Long? = null,
    val verificationStatus: String = "PENDING",
    val rejectionReason: String? = null,
    val verifiedAt: String? = null,
    val createdAt: String? = null
)
