package net.corda.node.cordapp

import net.corda.core.node.services.AttachmentStorage
import net.corda.node.internal.cordapp.CordappLoader
import net.corda.node.internal.cordapp.CordappProvider
import net.corda.testing.node.MockAttachmentStorage
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CordappProviderTests {
    companion object {
        private val isolatedJAR = this::class.java.getResource("isolated.jar")!!
        private val emptyJAR = this::class.java.getResource("empty.jar")!!
    }

    private lateinit var attachmentStore: AttachmentStorage

    @Before
    fun setup() {
        attachmentStore = MockAttachmentStorage()
    }

    @Test
    fun `isolated jar is loaded into the attachment store`() {
        val loader = CordappLoader.createDevMode(listOf(isolatedJAR))
        val provider = CordappProvider(attachmentStore, loader)

        provider.start()
        val maybeAttachmentId = provider.getCordappAttachmentId(provider.cordapps.first())

        Assert.assertNotNull(maybeAttachmentId)
        Assert.assertNotNull(attachmentStore.openAttachment(maybeAttachmentId!!))
    }

    @Test
    fun `empty jar is not loaded into the attachment store`() {
        val loader = CordappLoader.createDevMode(listOf(emptyJAR))
        val provider = CordappProvider(attachmentStore, loader)

        provider.start()
        
        Assert.assertNull(provider.getCordappAttachmentId(provider.cordapps.first()))
    }

    @Test
    fun `test that we find a cordapp class that is loaded into the store`() {
        val loader = CordappLoader.createDevMode(listOf(isolatedJAR))
        val provider = CordappProvider(attachmentStore, loader)
        val className = "net.corda.finance.contracts.isolated.AnotherDummyContract"

        val expected = provider.cordapps.first()
        val actual = provider.getCordappForClass(className)

        Assert.assertNotNull(actual)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `test that we find an attachment for a cordapp contrat class`() {
        val loader = CordappLoader.createDevMode(listOf(isolatedJAR))
        val provider = CordappProvider(attachmentStore, loader)
        val className = "net.corda.finance.contracts.isolated.AnotherDummyContract"

        provider.start()
        val expected = provider.getAppContext(provider.cordapps.first()).attachmentId
        val actual = provider.getContractAttachmentID(className)

        Assert.assertNotNull(actual)
        Assert.assertEquals(actual!!, expected)
    }
}
