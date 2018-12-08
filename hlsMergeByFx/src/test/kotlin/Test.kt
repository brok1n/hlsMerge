import com.brok1n.kotlin.hlsmerge.utils.log
import java.io.File
import java.io.FilenameFilter

fun main(args: Array<String>) {

    val path = "N:\\Video\\av"

//    File(path).listFiles().forEach {
//        if ( it.isDirectory ) {
//            val size = it.listFiles().size
//            "size:$size".log()
//            if ( size == 0 ) {
//                it.delete()
//            }
//        }
//    }


    if ( true ) {
        return
    }

    File(path).listFiles().forEach {
        if ( it.isDirectory ) {
            val fileSize = it.listFiles(FilenameFilter { dir, name ->
                var status = false
                if ( name.contains("_out") ) {
                    status = true
                }
                status
            }).size
            "fileSize:$fileSize".log()
            if ( fileSize == 1 ) {
                it.listFiles().forEach {
                    print(it)
                    val target = File(it.parentFile.parentFile, it.name)
                    it.renameTo(target)
                }
            }
        }
    }


}
