package com.washgo.agent.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AgentDocumentDtoTest {

    private val gson = Gson()

    @Test
    fun agentDocumentDto_serializesAndDeserializesCorrectly() {
        val json = """
            {
                "id": "doc-uuid-1234",
                "agentId": "agent-uuid-5678",
                "documentType": "GOVERNMENT_ID",
                "documentNumber": "1234-5678-9012",
                "fileName": "aadhaar_card.pdf",
                "contentType": "application/pdf",
                "fileSize": 1048576,
                "verificationStatus": "VERIFIED",
                "rejectionReason": null,
                "verifiedAt": "2026-10-14T10:00:00Z",
                "createdAt": "2026-10-14T09:00:00Z"
            }
        """.trimIndent()

        val dto = gson.fromJson(json, AgentDocumentDto::class.java)

        assertNotNull(dto)
        assertEquals("doc-uuid-1234", dto.id)
        assertEquals("agent-uuid-5678", dto.agentId)
        assertEquals("GOVERNMENT_ID", dto.documentType)
        assertEquals("1234-5678-9012", dto.documentNumber)
        assertEquals("aadhaar_card.pdf", dto.fileName)
        assertEquals("application/pdf", dto.contentType)
        assertEquals(1048576L, dto.fileSize)
        assertEquals("VERIFIED", dto.verificationStatus)
        assertNull(dto.rejectionReason)
        assertEquals("2026-10-14T10:00:00Z", dto.verifiedAt)
    }

    @Test
    fun documentTypes_containAllBackendSupportedCategories() {
        val types = DocumentType.values().map { it.name }
        assertEquals(
            listOf(
                "GOVERNMENT_ID",
                "DRIVING_LICENSE",
                "VEHICLE_REGISTRATION",
                "POLICE_CLEARANCE",
                "INSURANCE"
            ),
            types
        )
    }

    @Test
    fun documentVerificationStatuses_coverAllBackendTransitions() {
        val statuses = DocumentVerificationStatus.values().map { it.name }
        assertEquals(listOf("PENDING", "VERIFIED", "REJECTED"), statuses)
    }
}
