package moe.nea.licenseextractificator

import com.google.gson.GsonBuilder
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class JsonLicenseFormatter(var charset: Charset = StandardCharsets.UTF_8) : LicenseFormatter, Serializable {
    companion object {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
    }

    @Throws(IOException::class)
    private fun writeObject(objectOutputStream: ObjectOutputStream) {
        objectOutputStream.writeUTF(charset.name())
    }

    @Throws(IOException::class)
    private fun readObject(objectInputStream: ObjectInputStream) {
        charset = Charset.forName(objectInputStream.readUTF())
    }


    override fun formatLicense(licenses: List<ProjectLicensing>, outputFile: File) {
        outputFile.writer(charset).use {
            gson.toJson(licenses, it)
        }
    }
}
