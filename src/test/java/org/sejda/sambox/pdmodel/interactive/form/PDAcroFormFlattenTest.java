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
package org.sejda.sambox.pdmodel.interactive.form;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.sejda.io.SeekableSources;
import org.sejda.sambox.input.PDFParser;
import org.sejda.sambox.pdmodel.PDDocument;
import org.sejda.sambox.rendering.TestPDFToImage;

/**
 * Test flatten different forms and compare with rendering.
 *
 * The tests are currently disabled to not run within the CI environment as the test results need manual inspection.
 * Enable as needed.
 *
 */
public class PDAcroFormFlattenTest
{

    private static final File TARGETPDFDIR = new File("target/pdfs");
    private static final File IN_DIR = new File("target/test-output/flatten/in");
    private static final File OUT_DIR = new File("target/test-output/flatten/out");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /*
     * PDFBOX-142 Filled template.
     */
    // @Test
    public void testFlattenPDFBOX142() throws IOException
    {
        flattenAndCompare("Testformular1.pdf");
    }

    /*
     * PDFBOX-563 Filled template.
     */
    // @Test
    public void testFlattenPDFBOX563() throws IOException
    {
        flattenAndCompare("TestFax_56972.pdf");
    }

    /*
     * PDFBOX-2469 Empty template.
     */
    // @Test
    public void testFlattenPDFBOX2469Empty() throws IOException
    {
        flattenAndCompare("FormI-9-English.pdf");
    }

    /*
     * PDFBOX-2469 Filled template.
     */
    // @Test
    public void testFlattenPDFBOX2469Filled() throws IOException
    {
        flattenAndCompare("testPDF_acroForm.pdf");
    }

    /*
     * PDFBOX-2586 Empty template.
     */
    // @Test
    public void testFlattenPDFBOX2586() throws IOException
    {
        flattenAndCompare("test-2586.pdf");
    }

    /*
     * PDFBOX-3083 Filled template rotated.
     */
    // @Test
    public void testFlattenPDFBOX3083() throws IOException
    {
        flattenAndCompare("mypdf.pdf");
    }

    /*
     * PDFBOX-3262 Hidden fields
     */
    // @Test
    public void testFlattenPDFBOX3262() throws IOException
    {
        assertTrue(flattenAndCompare("hidden_fields.pdf"));
    }

    /*
     * PDFBOX-3396 Signed Document 1.
     */
    // @Test
    public void testFlattenPDFBOX3396_1() throws IOException
    {
        flattenAndCompare("Signed-Document-1.pdf");
    }

    /*
     * PDFBOX-3396 Signed Document 2.
     */
    // @Test
    public void testFlattenPDFBOX3396_2() throws IOException
    {
        flattenAndCompare("Signed-Document-2.pdf");
    }

    /*
     * PDFBOX-3396 Signed Document 3.
     */
    // @Test
    public void testFlattenPDFBOX3396_3() throws IOException
    {
        flattenAndCompare("Signed-Document-3.pdf");
    }

    /*
     * PDFBOX-3396 Signed Document 4.
     */
    // @Test
    public void testFlattenPDFBOX3396_4() throws IOException
    {
        flattenAndCompare("Signed-Document-4.pdf");
    }

    /*
     * PDFBOX-3587 Empty template.
     */
    // @Test
    public void testFlattenOpenOfficeForm() throws IOException
    {
        flattenAndCompare("OpenOfficeForm.pdf");
    }

    /*
     * PDFBOX-3587 Filled template.
     */
    // @Test
    public void testFlattenOpenOfficeFormFilled() throws IOException
    {
        flattenAndCompare("OpenOfficeForm_filled.pdf");
    }

    /**
     * PDFBOX-4157 Filled template.
     */
    // @Test
    public void testFlattenPDFBox4157() throws IOException
    {
        flattenAndCompare("PDFBOX-4157-filled.pdf");
    }

    /**
     * PDFBOX-4172 Filled template.
     */
    // @Test
    public void testFlattenPDFBox4172() throws IOException
    {
        flattenAndCompare("PDFBOX-4172-filled.pdf");
    }

    /**
     * PDFBOX-4615 Filled template.
     */
    // @Test
    public void testFlattenPDFBox4615() throws IOException
    {
        flattenAndCompare("resetboundingbox-filled.pdf");
    }

    /**
     * PDFBOX-4693: page is not rotated, but the appearance stream is.
     */
    // @Test
    public void testFlattenPDFBox4693() throws IOException
    {

        flattenAndCompare("stenotypeTest-3_rotate_no_flatten.pdf");
    }

    /*
     * Flatten and compare with generated image samples.
     */
    private boolean flattenAndCompare(String fileName) throws IOException
    {
        File inputFile = new File(IN_DIR, fileName);
        File outputFile = new File(OUT_DIR, fileName);

        try (PDDocument doc = PDFParser
                .parse(SeekableSources.seekableSourceFrom(new File(TARGETPDFDIR, fileName))))
        {
            doc.getDocumentCatalog().getAcroForm().flatten();
            assertTrue(doc.getDocumentCatalog().getAcroForm().getFields().isEmpty());
            doc.writeTo(outputFile);
        }

        // compare rendering
        TestPDFToImage testPDFToImage = new TestPDFToImage(TestPDFToImage.class.getName());
        if (!testPDFToImage.doTestFile(outputFile, IN_DIR.getAbsolutePath(),
                OUT_DIR.getAbsolutePath()))
        {
            // don't fail, rendering is different on different systems, result must be viewed manually
            System.out.println("Rendering of " + outputFile
                    + " failed or is not identical to expected rendering in " + IN_DIR
                    + " directory");
            removeMatchingRenditions(inputFile);
            return false;
        }
        // cleanup input and output directory for matching files.
        removeAllRenditions(inputFile);

        return true;
    }

    /*
     * Remove renditions for the PDF from the input directory for which there is no corresponding rendition in the
     * output directory. Renditions in the output directory which were identical to the ones in the input directory will
     * have been deleted by the TestPDFToImage utility.
     */
    private static void removeMatchingRenditions(final File inputFile)
    {
        File[] testFiles = inputFile.getParentFile().listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return (name.startsWith(inputFile.getName())
                        && name.toLowerCase().endsWith(".png"));
            }
        });

        for (File testFile : testFiles)
        {
            if (!new File(OUT_DIR, testFile.getName()).exists())
            {
                testFile.delete();
            }
        }
    }

    /*
     * Remove renditions for the PDF from the input directory. The output directory will have been cleaned by the
     * TestPDFToImage utility.
     */
    private static void removeAllRenditions(final File inputFile)
    {
        File[] testFiles = inputFile.getParentFile().listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return (name.startsWith(inputFile.getName())
                        && name.toLowerCase().endsWith(".png"));
            }
        });

        for (File testFile : testFiles)
        {
            testFile.delete();
        }
    }
}
