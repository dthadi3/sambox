/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sejda.sambox.pdmodel.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import org.junit.Before;
import org.junit.Test;
import org.sejda.sambox.cos.*;
import org.sejda.sambox.pdmodel.PDDocument;
import org.sejda.sambox.pdmodel.PDPage;
import org.sejda.sambox.pdmodel.interactive.annotation.AnnotationFilter;
import org.sejda.sambox.pdmodel.interactive.annotation.PDAnnotation;
import org.sejda.sambox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.sejda.sambox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
import org.sejda.sambox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;

import static org.junit.Assert.*;

public class COSArrayListTest
{
    // next two entries are to be used for comparison with
    // COSArrayList behaviour in order to ensure that the
    // intented object is now at the correct position.
    // Will also be used for Collection/Array based setting
    // and comparison
    private List<PDAnnotation> tbcAnnotationsList;
    private COSBase[] tbcAnnotationsArray;

    // next entries are to be used within COSArrayList
    private List<PDAnnotation> annotationsList;
    private COSArray annotationsArray;

    // to be used when testing retrieving filtered items as can be done with
    // {@link PDPage.getAnnotations(AnnotationFilter annotationFilter)}
    private PDPage pdPage;

    private File tmpFile;

    /**
     * Create thre new different annotations an add them to the Java List/Array as well as PDFBox List/Array
     * implementations.
     */
    @Before
    public void setUp() throws IOException {
        annotationsList = new ArrayList<>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(
                PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();
        PDAnnotationLink txtLink2 = new PDAnnotationLink();
        PDAnnotationSquareCircle aCircle = new PDAnnotationSquareCircle(
                PDAnnotationSquareCircle.SUB_TYPE_CIRCLE);

        annotationsList.add(txtMark);
        annotationsList.add(txtLink);
        annotationsList.add(aCircle);
        annotationsList.add(txtLink2);
        assertTrue(annotationsList.size() == 4);

        tbcAnnotationsList = new ArrayList<PDAnnotation>();
        tbcAnnotationsList.add(txtMark);
        tbcAnnotationsList.add(txtLink);
        tbcAnnotationsList.add(aCircle);
        tbcAnnotationsList.add(txtLink2);
        assertTrue(tbcAnnotationsList.size() == 4);

        annotationsArray = new COSArray();
        annotationsArray.add(txtMark);
        annotationsArray.add(txtLink);
        annotationsArray.add(aCircle);
        annotationsArray.add(txtLink2);
        assertTrue(annotationsArray.size() == 4);

        tbcAnnotationsArray = new COSBase[4];
        tbcAnnotationsArray[0] = txtMark.getCOSObject();
        tbcAnnotationsArray[1] = txtLink.getCOSObject();
        tbcAnnotationsArray[2] = aCircle.getCOSObject();
        tbcAnnotationsArray[3] = txtLink2.getCOSObject();
        assertTrue(tbcAnnotationsArray.length == 4);

        // add the annotations to the page
        pdPage = new PDPage();
        pdPage.setAnnotations(annotationsList);


        tmpFile = File.createTempFile("tmp", ".pdf");
        tmpFile.deleteOnExit();
    }

    /**
     * Test getting a PDModel element is in sync with underlying COSArray
     */
    @Test
    public void getFromList() {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<>(annotationsList, annotationsArray);

        for (int i = 0; i < cosArrayList.size(); i++) {
            PDAnnotation annot = (PDAnnotation) cosArrayList.get(i);
            assertTrue("PDAnnotations cosObject at " + i + " shall be equal to index " + i + " of COSArray",
                    annotationsArray.get(i).equals(annot.getCOSObject()));

            // compare with Java List/Array
            assertTrue("PDAnnotations at " + i + " shall be at index " + i + " of List",
                    tbcAnnotationsList.get(i).equals((annot)));
            assertEquals("PDAnnotations cosObject at " + i + " shall be at position " + i + " of Array",
                    tbcAnnotationsArray[i], annot.getCOSObject());
        }
    }

    /**
     * Test adding a PDModel element is in sync with underlying COSArray
     */
    @Test
    public void addToList() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        // add new annotation
        PDAnnotationSquareCircle aSquare = new PDAnnotationSquareCircle(PDAnnotationSquareCircle.SUB_TYPE_SQUARE);
        cosArrayList.add(aSquare);

        assertTrue("List size shall be 5", annotationsList.size() == 5);
        assertTrue("COSArray size shall be 5", annotationsArray.size() == 5);

        PDAnnotation annot = (PDAnnotation) annotationsList.get(4);
        assertTrue("Added annotation shall be 4th entry in COSArray", annotationsArray.indexOf(annot.getCOSObject()) == 4);
        assertEquals("Provided COSArray and underlying COSArray shall be equal", annotationsArray, cosArrayList.getCOSArray());
    }

    /**
     * Test removing a PDModel element by index is in sync with underlying COSArray
     */
    @Test
    public void removeFromListByIndex() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        int positionToRemove = 2;
        PDAnnotation toBeRemoved = cosArrayList.get(positionToRemove);

        assertEquals("Remove operation shall return the removed object",toBeRemoved, cosArrayList.remove(positionToRemove));
        assertTrue("List size shall be 3", cosArrayList.size() == 3);
        assertTrue("COSArray size shall be 3", annotationsArray.size() == 3);

        assertTrue("PDAnnotation shall no longer exist in List",
                cosArrayList.indexOf(tbcAnnotationsList.get(positionToRemove)) == -1);
        assertTrue("COSObject shall no longer exist in COSArray",
                annotationsArray.indexOf(tbcAnnotationsArray[positionToRemove]) == -1);
    }

    /**
     * Test removing a unique PDModel element by index is in sync with underlying COSArray
     */
    @Test
    public void removeUniqueFromListByObject() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        int positionToRemove = 2;
        PDAnnotation toBeRemoved = annotationsList.get(positionToRemove);

        assertTrue("Remove operation shall return true",cosArrayList.remove(toBeRemoved));
        assertTrue("List size shall be 3", cosArrayList.size() == 3);
        assertTrue("COSArray size shall be 3", annotationsArray.size() == 3);

        // compare with Java List/Array to ensure correct object at position
        assertTrue("List object at 3 is at position 2 in COSArrayList now",
                cosArrayList.get(2).equals(tbcAnnotationsList.get(3)));
        assertTrue("COSObject of List object at 3 is at position 2 in COSArray now",
                annotationsArray.get(2).equals(tbcAnnotationsList.get(3).getCOSObject()));
        assertTrue("Array object at 3 is at position 2 in underlying COSArray now",
                annotationsArray.get(2).equals(tbcAnnotationsArray[3]));

        assertTrue("PDAnnotation shall no longer exist in List",
                cosArrayList.indexOf(tbcAnnotationsList.get(positionToRemove)) == -1);
        assertTrue("COSObject shall no longer exist in COSArray",
                annotationsArray.indexOf(tbcAnnotationsArray[positionToRemove]) == -1);

        assertFalse("Remove shall not remove any object",cosArrayList.remove(toBeRemoved));

    }

    /**
     * Test removing a unique PDModel element by index is in sync with underlying COSArray
     */
    @Test
    public void removeAllUniqueFromListByObject() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        int positionToRemove = 2;
        PDAnnotation toBeRemoved = annotationsList.get(positionToRemove);

        List<PDAnnotation> toBeRemovedInstances = Collections.singletonList(toBeRemoved);

        assertTrue("Remove operation shall return true",cosArrayList.removeAll(toBeRemovedInstances));
        assertTrue("List size shall be 3", cosArrayList.size() == 3);
        assertTrue("COSArray size shall be 3", annotationsArray.size() == 3);

        assertFalse("Remove shall not remove any object",cosArrayList.removeAll(toBeRemovedInstances));
    }

    /**
     * Test removing a unique PDModel element by index is in sync with underlying COSArray
     */
    @Test
    public void removeAllMultipleFromListByObject() throws Exception {
        COSArrayList<PDAnnotation> cosArrayList = new COSArrayList<PDAnnotation>(annotationsList, annotationsArray);

        int positionToRemove = 1;
        PDAnnotation toBeRemoved = annotationsList.get(positionToRemove);

        List<PDAnnotation> toBeRemovedInstances = Collections.singletonList(toBeRemoved);

        assertTrue("Remove operation shall return true",cosArrayList.removeAll(toBeRemovedInstances));
        assertTrue("List size shall be 3", cosArrayList.size() == 3);
        assertTrue("COSArray size shall be 3", annotationsArray.size() == 3);

        assertFalse("Remove shall not remove any object",cosArrayList.removeAll(toBeRemovedInstances));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeFromFilteredListByIndex() throws Exception
    {
        // retrieve all annotations from page but the link annotation
        // which is 2nd in list - see above setup
        AnnotationFilter annotsFilter = new AnnotationFilter()
        {
            @Override
            public boolean accept(PDAnnotation annotation)
            {
                return !(annotation instanceof PDAnnotationLink);
            }
        };

        COSArrayList<PDAnnotation> cosArrayList = (COSArrayList<PDAnnotation>) pdPage.getAnnotations(annotsFilter);

        // this call should fail
        cosArrayList.remove(1);
    }


    @Test(expected = UnsupportedOperationException.class)
    public void removeFromFilteredListByObject() throws Exception
    {
        // retrieve all annotations from page but the link annotation
        // which is 2nd in list - see above setup
        AnnotationFilter annotsFilter = new AnnotationFilter()
        {
            @Override
            public boolean accept(PDAnnotation annotation)
            {
                return !(annotation instanceof PDAnnotationLink);
            }
        };

        COSArrayList<PDAnnotation> cosArrayList = (COSArrayList<PDAnnotation>) pdPage.getAnnotations(annotsFilter);

        // remove object
        int positionToRemove = 1;
        PDAnnotation toBeRemoved = cosArrayList.get(positionToRemove);

        // this call should fail
        cosArrayList.remove(toBeRemoved);

    }

    @Test
    public void removeSingleDirectObject() throws IOException {

        // generate test file
        PDDocument pdf = new PDDocument();

        PDPage page = new PDPage();
        pdf.addPage(page);

        ArrayList<PDAnnotation> pageAnnots = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();

        // TODO: enforce the COSDictionaries to be written directly into the COSArray
//        txtMark.getCOSObject().getCOSObject().setDirect(true);
//        txtLink.getCOSObject().getCOSObject().setDirect(true);

        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtLink);
        assertTrue("There shall be 4 annotations generated", pageAnnots.size() == 4);

        page.setAnnotations(pageAnnots);

        pdf.writeTo(tmpFile);
        pdf.close();

        pdf = PDDocument.load(tmpFile);
        page = pdf.getPage(0);

        COSArrayList<PDAnnotation> annotations = (COSArrayList) page.getAnnotations();

        assertTrue("There shall be 4 annotations retrieved", annotations.size() == 4);
        assertTrue("The size of the internal COSArray shall be 4", annotations.getCOSArray().size() == 4);

        PDAnnotation toBeRemoved = annotations.get(0);
        annotations.remove(toBeRemoved);

        assertTrue("There shall be 3 annotations left", annotations.size() == 3);
        assertTrue("The size of the internal COSArray shall be 3", annotations.getCOSArray().size() == 3);
        pdf.close();
    }

    @Test
    public void removeSingleIndirectObject() throws IOException {

        // generate test file
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        ArrayList<PDAnnotation> pageAnnots = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();

        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtLink);
        assertTrue("There shall be 4 annotations generated", pageAnnots.size() == 4);

        page.setAnnotations(pageAnnots);

        pdf.writeTo(tmpFile);
        pdf.close();

        pdf = PDDocument.load(tmpFile);
        page = pdf.getPage(0);

        List<PDAnnotation> annotations = page.getAnnotations();
        COSArrayList<PDAnnotation> cosArrayListAnnotations = (COSArrayList<PDAnnotation>) annotations;

        assertTrue("There shall be 4 annotations retrieved", annotations.size() == 4);
        assertTrue("The size of the internal COSArray shall be 4", cosArrayListAnnotations.getCOSArray().size() == 4);

        PDAnnotation toBeRemoved = annotations.get(0);

        annotations.remove(toBeRemoved);

        assertTrue("There shall be 3 annotations left", annotations.size() == 3);
        assertTrue("The size of the internal COSArray shall be 3", cosArrayListAnnotations.getCOSArray().size() == 3);

        page.setAnnotations(annotations);
        assertTrue("There shall be 3 annotations left on page", page.getAnnotations().size() == 3);

        pdf.close();
    }

    @Test
    public void removeDirectObject() throws IOException {

        // generate test file
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        ArrayList<PDAnnotation> pageAnnots = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();

        // TODO: enforce the COSDictionaries to be written directly into the COSArray
//        txtMark.getCOSObject().getCOSObject().setDirect(true);
//        txtLink.getCOSObject().getCOSObject().setDirect(true);

        pageAnnots.add(txtMark);
        pageAnnots.add(txtLink);
        assertTrue("There shall be 2 annotations generated", pageAnnots.size() == 2);

        page.setAnnotations(pageAnnots);

        pdf.writeTo(tmpFile);
        pdf.close();

        pdf = PDDocument.load(tmpFile);
        page = pdf.getPage(0);

        COSArrayList<PDAnnotation> annotations = (COSArrayList) page.getAnnotations();

        assertTrue("There shall be 2 annotations retrieved", annotations.size() == 2);
        assertTrue("The size of the internal COSArray shall be 2", annotations.getCOSArray().size() == 2);

        ArrayList<PDAnnotation> toBeRemoved = new ArrayList<PDAnnotation>();

        toBeRemoved.add(annotations.get(0));
        annotations.removeAll(toBeRemoved);

        assertTrue("There shall be 1 annotations left", annotations.size() == 1);
        assertTrue("The size of the internal COSArray shall be 1", annotations.getCOSArray().size() == 1);
    }

    @Test
    public void removeIndirectObject() throws IOException {

        // generate test file
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        ArrayList<PDAnnotation> pageAnnots = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();

        pageAnnots.add(txtMark);
        pageAnnots.add(txtLink);
        assertTrue("There shall be 2 annotations generated", pageAnnots.size() == 2);

        page.setAnnotations(pageAnnots);

        pdf.writeTo(tmpFile);
        pdf.close();

        pdf = PDDocument.load(tmpFile);
        page = pdf.getPage(0);

        COSArrayList<PDAnnotation> annotations = (COSArrayList) page.getAnnotations();

        assertTrue("There shall be 2 annotations retrieved", annotations.size() == 2);
        assertTrue("The size of the internal COSArray shall be 2", annotations.getCOSArray().size() == 2);

        ArrayList<PDAnnotation> toBeRemoved = new ArrayList<PDAnnotation>();
        toBeRemoved.add(annotations.get(0));

        annotations.removeAll(toBeRemoved);

        assertTrue("There shall be 1 annotations left", annotations.size() == 1);
        assertTrue("The size of the internal COSArray shall be 1", annotations.getCOSArray().size() == 1);
    }

    @Test
    public void retainDirectObject() throws IOException {

        // generate test file
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        ArrayList<PDAnnotation> pageAnnots = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();

        // TODO: enforce the COSDictionaries to be written directly into the COSArray
//        txtMark.getCOSObject().getCOSObject().setDirect(true);
//        txtLink.getCOSObject().getCOSObject().setDirect(true);

        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtLink);
        assertTrue("There shall be 4 annotations generated", pageAnnots.size() == 4);

        page.setAnnotations(pageAnnots);

        pdf.writeTo(tmpFile);
        pdf.close();

        pdf = PDDocument.load(tmpFile);
        page = pdf.getPage(0);

        COSArrayList<PDAnnotation> annotations = (COSArrayList) page.getAnnotations();

        assertTrue("There shall be 4 annotations retrieved", annotations.size() == 4);
        assertTrue("The size of the internal COSArray shall be 4", annotations.getCOSArray().size() == 4);

        ArrayList<PDAnnotation> toBeRetained = new ArrayList<PDAnnotation>();

        toBeRetained.add(annotations.get(0));
        annotations.retainAll(toBeRetained);

        assertTrue("There shall be 3 annotations left", annotations.size() == 3);
        assertTrue("The size of the internal COSArray shall be 3", annotations.getCOSArray().size() == 3);
    }

    @Test
    public void retainIndirectObject() throws IOException {

        // generate test file
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        ArrayList<PDAnnotation> pageAnnots = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();

        // TODO: enforce the COSDictionaries to be written directly into the COSArray
//        txtMark.getCOSObject().getCOSObject().setDirect(true);
//        txtLink.getCOSObject().getCOSObject().setDirect(true);

        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtMark);
        pageAnnots.add(txtLink);
        assertTrue("There shall be 4 annotations generated", pageAnnots.size() == 4);

        page.setAnnotations(pageAnnots);

        pdf.writeTo(tmpFile);
        pdf.close();

        pdf = PDDocument.load(tmpFile);
        page = pdf.getPage(0);

        COSArrayList<PDAnnotation> annotations = (COSArrayList) page.getAnnotations();

        assertTrue("There shall be 4 annotations retrieved", annotations.size() == 4);
        assertTrue("The size of the internal COSArray shall be 4", annotations.getCOSArray().size() == 4);

        ArrayList<PDAnnotation> toBeRetained = new ArrayList<PDAnnotation>();

        toBeRetained.add(annotations.get(0));
        annotations.retainAll(toBeRetained);

        assertTrue("There shall be 3 annotations left", annotations.size() == 3);
        assertTrue("The size of the internal COSArray shall be 3", annotations.getCOSArray().size() == 3);
    }

    @Test
    public void removingAnnotationFromPageViaSetAnnotations() throws IOException {

        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);

        ArrayList<PDAnnotation> pageAnnots = new ArrayList<PDAnnotation>();
        PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        PDAnnotationLink txtLink = new PDAnnotationLink();

        pageAnnots.add(txtMark);
        pageAnnots.add(txtLink);

        page.setAnnotations(pageAnnots);

        pdf.writeTo(tmpFile);
        pdf.close();

        pdf = PDDocument.load(tmpFile);
        page = pdf.getPage(0);

        List<PDAnnotation> annotations = page.getAnnotations();
        assertThat(annotations.size(), is(2));
        annotations.remove(annotations.get(1));

        page.setAnnotations(annotations);

        assertThat(page.getAnnotations().size(), is(1));
    }
}
