/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse

import org.pih.warehouse.util.ImageUtil;

class ResizeImageTagLib { 
	
	def resizeImage = { attrs, body ->
		//def file = downloadFile(attrs.src)
		//out << file.absolutePath
		//ImageUtil.resizeImage(file.bytes, out, 200, 200);
		def out = new FileOutputStream("/tmp/image.jpg")
		ImageUtil.resizeImage(attrs.src, out, 200, 200);
	}	
	
	def downloadFile(url) {
		def filename = url.tokenize("/")[-1]
		def fileOutputStream = new FileOutputStream(filename)
		def out = new BufferedOutputStream(fileOutputStream)
		out << new URL(url).openStream()
		out.close()
		return new File(filename)
	}
	
	
}