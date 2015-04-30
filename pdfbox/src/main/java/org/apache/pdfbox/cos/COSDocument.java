/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.cos;

import static org.apache.pdfbox.util.RequireUtils.requireNotNullArg;

import java.io.IOException;
import java.util.Optional;

/**
 * This is the in-memory representation of the PDF document.
 *
 * @author Ben Litchfield
 * 
 */
public final class COSDocument extends COSBase
{

    private float headerVersion;
    private COSDictionary trailer;
    private boolean xRefStream;

    public COSDocument()
    {
        this(new COSDictionary(), 1.4f);
        COSDictionary catalog = new COSDictionary();
        catalog.setItem(COSName.TYPE, COSName.CATALOG);
        trailer.setItem(COSName.ROOT, catalog);
    }

    public COSDocument(COSDictionary trailer)
    {
        this(trailer, 1.4f);
    }

    // TODO use an enum instead of float
    public COSDocument(COSDictionary trailer, float headerVersion)
    {
        requireNotNullArg(trailer, "Trailer cannot be null");
        this.trailer = trailer;
        this.headerVersion = headerVersion;
    }

    public void setHeaderVersion(float headerVersion)
    {
        this.headerVersion = headerVersion;
    }

    public float getHeaderVersion()
    {
        return headerVersion;
    }

    /**
     * @return true If this document has an encryption dictionary
     */
    public boolean isEncrypted()
    {
        return trailer.getDictionaryObject(COSName.ENCRYPT) != null;
    }

    /**
     * This will get the encryption dictionary if the document is encrypted or null if the document is not encrypted.
     *
     * @return The encryption dictionary.
     */
    public COSDictionary getEncryptionDictionary()
    {
        return (COSDictionary) trailer.getDictionaryObject(COSName.ENCRYPT);
    }

    /**
     * This will set the encryption dictionary, this should only be called when encrypting the document.
     *
     * @param encDictionary The encryption dictionary.
     */
    public void setEncryptionDictionary(COSDictionary encDictionary)
    {
        trailer.setItem(COSName.ENCRYPT, encDictionary);
    }

    public COSArray getDocumentID()
    {
        return (COSArray) trailer.getDictionaryObject(COSName.ID);
    }

    public void setDocumentID(COSArray id)
    {
        trailer.setItem(COSName.ID, id);
    }

    /**
     * @return the catalog for this document
     * @throws IllegalStateException If no catalog can be found.
     */
    public COSDictionary getCatalog()
    {
        return (COSDictionary) Optional.ofNullable(trailer.getDictionaryObject(COSName.ROOT))
                .map(COSBase::getCOSObject)
                .orElseThrow(() -> new IllegalStateException("Catalog cannot be found"));
    }

    public COSDictionary getTrailer()
    {
        return trailer;
    }

    /**
     * @return true if the trailer is a XRef stream
     */
    public boolean isXRefStream()
    {
        return xRefStream;
    }

    /**
     * @param xRefStream the new value for xRefStream
     */
    public void setIsXRefStream(boolean xRefStream)
    {
        this.xRefStream = xRefStream;
    }

    @Override
    public void accept(COSVisitor visitor) throws IOException
    {
        visitor.visit(this);
    }
}
