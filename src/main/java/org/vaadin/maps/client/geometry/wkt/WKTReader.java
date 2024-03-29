/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package org.vaadin.maps.client.geometry.wkt;

import com.vividsolutions.jts.geom.GeometryFactory;
import org.vaadin.maps.client.emul.io.IOException;
import org.vaadin.maps.client.emul.io.Reader;
import org.vaadin.maps.client.emul.io.StreamTokenizer;
import org.vaadin.maps.client.emul.io.StringReader;
import org.vaadin.maps.client.geometry.*;
import org.vaadin.maps.client.geometry.util.Assert;
import org.vaadin.maps.client.geometry.util.AssertionFailedException;
import org.vaadin.maps.client.io.ParseException;

import java.util.ArrayList;

/**
 * Converts a geometry in Well-Known Text format to a {@link Geometry}.
 * <p>
 * <code>WKTReader</code> supports extracting <code>Geometry</code> objects from
 * either {@link Reader}s or {@link String}s. This allows it to function as a
 * parser to read <code>Geometry</code> objects from text blocks embedded in
 * other data formats (e.g. XML).
 * <p>
 * <p>
 * A <code>WKTReader</code> is parameterized by a <code>GeometryFactory</code>,
 * to allow it to create <code>Geometry</code> objects of the appropriate
 * implementation. In particular, the <code>GeometryFactory</code> determines
 * the <code>PrecisionModel</code> and <code>SRID</code> that is used.
 * <p>
 * <p>
 * The <code>WKTReader</code> converts all input numbers to the precise internal
 * representation.
 *
 * <h3>Notes:</h3>
 * <ul>
 * <li>The reader supports non-standard "LINEARRING" tags.
 * <li>The reader uses Double.parseDouble to perform the conversion of ASCII
 * numbers to floating point. This means it supports the Java syntax for
 * floating point literals (including scientific notation).
 * </ul>
 *
 * <h3>Syntax</h3> The following syntax specification describes the version of
 * Well-Known Text supported by JTS. (The specification uses a syntax language
 * similar to that used in the C and Java language specifications.)
 * <p>
 *
 * <blockquote>
 *
 * <pre>
 * <i>WKTGeometry:</i> one of<i>
 *
 *       WKTPoint  WKTLineString  WKTLinearRing  WKTPolygon
 *       WKTMultiPoint  WKTMultiLineString  WKTMultiPolygon
 *       WKTGeometryCollection</i>
 *
 * <i>WKTPoint:</i> <b>POINT ( </b><i>Coordinate</i> <b>)</b>
 *
 * <i>WKTLineString:</i> <b>LINESTRING</b> <i>CoordinateSequence</i>
 *
 * <i>WKTLinearRing:</i> <b>LINEARRING</b> <i>CoordinateSequence</i>
 *
 * <i>WKTPolygon:</i> <b>POLYGON</b> <i>CoordinateSequenceList</i>
 *
 * <i>WKTMultiPoint:</i> <b>MULTIPOINT</b> <i>CoordinateSequence</i>
 *
 * <i>WKTMultiLineString:</i> <b>MULTILINESTRING</b> <i>CoordinateSequenceList</i>
 *
 * <i>WKTMultiPolygon:</i>
 *         <b>MULTIPOLYGON (</b> <i>CoordinateSequenceList {</i> , <i>CoordinateSequenceList }</i> <b>)</b>
 *
 * <i>WKTGeometryCollection: </i>
 *         <b>GEOMETRYCOLLECTION (</b> <i>WKTGeometry {</i> , <i>WKTGeometry }</i> <b>)</b>
 *
 * <i>CoordinateSequenceList:</i>
 *         <b>(</b> <i>CoordinateSequence {</i> <b>,</b> <i>CoordinateSequence }</i> <b>)</b>
 *
 * <i>CoordinateSequence:</i>
 *         <b>(</b> <i>Coordinate {</i> , <i>Coordinate }</i> <b>)</b>
 *
 * <i>Coordinate:
 *         Number Number Number<sub>opt</sub></i>
 *
 * <i>Number:</i> A Java-style floating-point number
 *
 * </pre>
 *
 * </blockquote>
 *
 * @version 1.7
 * @see WKTWriter
 */
public class WKTReader {
    private static final String EMPTY = "EMPTY";
    private static final String COMMA = ",";
    private static final String L_PAREN = "(";
    private static final String R_PAREN = ")";

    private static final String POINT = "POINT";
    private static final String LINESTRING = "LINESTRING";
    private static final String LINEARRING = "LINEARRING";
    private static final String POLYGON = "POLYGON";
    private static final String MULTIPOINT = "MULTIPOINT";
    private static final String MULTILINESTRING = "MULTILINESTRING";
    private static final String MULTIPOLYGON = "MULTIPOLYGON";
    private static final String GEOMETRYCOLLECTION = "GEOMETRYCOLLECTION";

    private StreamTokenizer tokenizer;

    /**
     * Creates a reader that creates objects using the default
     * {@link GeometryFactory}.
     */
    public WKTReader() {
    }

    /**
     * Reads a Well-Known Text representation of a {@link Geometry} from a
     * {@link String}.
     *
     * @param wellKnownText one or more <Geometry Tagged Text>strings (see the OpenGIS
     *                      Simple Features Specification) separated by whitespace
     * @return a <code>Geometry</code> specified by <code>wellKnownText</code>
     * @throws ParseException if a parsing problem occurs
     */
    public Geometry read(String wellKnownText) throws ParseException {
        StringReader reader = new StringReader(wellKnownText);
        try {
            return read(reader);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads a Well-Known Text representation of a {@link Geometry} from a
     * {@link Reader}.
     *
     * @param reader a Reader which will return a <Geometry Tagged Text> string
     *               (see the OpenGIS Simple Features Specification)
     * @return a <code>Geometry</code> read from <code>reader</code>
     * @throws ParseException if a parsing problem occurs
     */
    public Geometry read(Reader reader) throws ParseException {
        tokenizer = new StreamTokenizer(reader);
        // set tokenizer to NOT parse numbers
        tokenizer.resetSyntax();
        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('A', 'Z');
        tokenizer.wordChars(128 + 32, 255);
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('-', '-');
        tokenizer.wordChars('+', '+');
        tokenizer.wordChars('.', '.');
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.commentChar('#');

        try {
            return readGeometryTaggedText();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
    }

    /**
     * Returns the next array of <code>Coordinate</code>s in the stream.
     *
     * @return the next array of <code>Coordinate</code>s in the stream, or an
     * empty array if EMPTY is the next element returned by the stream.
     * @throws IOException    if an I/O error occurs
     * @throws ParseException if an unexpected token was encountered
     */
    private Coordinate[] getCoordinates() throws IOException, ParseException {
        String nextToken = getNextEmptyOrOpener();
        if (EMPTY.equals(nextToken)) {
            return new Coordinate[]{};
        }
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(getPreciseCoordinate());
        nextToken = getNextCloserOrComma();
        while (COMMA.equals(nextToken)) {
            coordinates.add(getPreciseCoordinate());
            nextToken = getNextCloserOrComma();
        }
        Coordinate[] array = new Coordinate[coordinates.size()];
        return coordinates.toArray(array);
    }

    private Coordinate getPreciseCoordinate() throws IOException, ParseException {
        Coordinate coordinate = new Coordinate();
        coordinate.x = getNextNumber();
        coordinate.y = getNextNumber();
        if (isNumberNext()) {
            // coordinate.z = getNextNumber();
            @SuppressWarnings("unused")
            double z = getNextNumber();
        }
        // precisionModel.makePrecise(coordinate);
        return coordinate;
    }

    private boolean isNumberNext() throws IOException {
        int type = tokenizer.nextToken();
        tokenizer.pushBack();
        return type == StreamTokenizer.TT_WORD;
    }

    /**
     * Parses the next number in the stream. Numbers with exponents are handled.
     *
     * @return the next number in the stream
     * @throws ParseException if the next token is not a valid number
     * @throws IOException    if an I/O error occurs
     */
    private double getNextNumber() throws IOException, ParseException {
        int type = tokenizer.nextToken();
        if (type == StreamTokenizer.TT_WORD) {
            try {
                return Double.parseDouble(tokenizer.sval);
            } catch (NumberFormatException ex) {
                throw new ParseException("Invalid number: " + tokenizer.sval);
            }
        }
        parseError("number");
        return 0.0;
    }

    /**
     * Returns the next EMPTY or L_PAREN in the stream as uppercase text.
     *
     * @return the next EMPTY or L_PAREN in the stream as uppercase text.
     * @throws ParseException if the next token is not EMPTY or L_PAREN
     * @throws IOException    if an I/O error occurs
     */
    private String getNextEmptyOrOpener() throws IOException, ParseException {
        String nextWord = getNextWord();
        if (EMPTY.equals(nextWord) || L_PAREN.equals(nextWord)) {
            return nextWord;
        }
        parseError(EMPTY + " or " + L_PAREN);
        return null;
    }

    /**
     * Returns the next R_PAREN or COMMA in the stream.
     *
     * @return the next R_PAREN or COMMA in the stream
     * @throws ParseException if the next token is not R_PAREN or COMMA
     * @throws IOException    if an I/O error occurs
     */
    private String getNextCloserOrComma() throws IOException, ParseException {
        String nextWord = getNextWord();
        if (COMMA.equals(nextWord) || R_PAREN.equals(nextWord)) {
            return nextWord;
        }
        parseError(COMMA + " or " + R_PAREN);
        return null;
    }

    /**
     * Returns the next R_PAREN in the stream.
     *
     * @return the next R_PAREN in the stream
     * @throws ParseException if the next token is not R_PAREN
     * @throws IOException    if an I/O error occurs
     */
    private String getNextCloser() throws IOException, ParseException {
        String nextWord = getNextWord();
        if (R_PAREN.equals(nextWord)) {
            return nextWord;
        }
        parseError(R_PAREN);
        return null;
    }

    /**
     * Returns the next word in the stream.
     *
     * @return the next word in the stream as uppercase text
     * @throws ParseException if the next token is not a word
     * @throws IOException    if an I/O error occurs
     */
    private String getNextWord() throws IOException, ParseException {
        int type = tokenizer.nextToken();
        switch (type) {
            case StreamTokenizer.TT_WORD:

                String word = tokenizer.sval;
                if (word.equalsIgnoreCase(EMPTY))
                    return EMPTY;
                return word;

            case '(':
                return L_PAREN;
            case ')':
                return R_PAREN;
            case ',':
                return COMMA;
        }
        parseError("word");
        return null;
    }

    /**
     * Throws a formatted ParseException for the current token.
     *
     * @param expected a description of what was expected
     * @throws ParseException
     * @throws AssertionFailedException if an invalid token is encountered
     */
    private void parseError(String expected) throws ParseException {
        // throws Asserts for tokens that should never be seen
        if (tokenizer.ttype == StreamTokenizer.TT_NUMBER)
            Assert.shouldNeverReachHere("Unexpected NUMBER token");
        if (tokenizer.ttype == StreamTokenizer.TT_EOL)
            Assert.shouldNeverReachHere("Unexpected EOL token");

        String tokenStr = tokenString();
        throw new ParseException("Expected " + expected + " but found " + tokenStr);
    }

    /**
     * Gets a description of the current token
     *
     * @return a description of the current token
     */
    private String tokenString() {
        switch (tokenizer.ttype) {
            case StreamTokenizer.TT_NUMBER:
                return "<NUMBER>";
            case StreamTokenizer.TT_EOL:
                return "End-of-Line";
            case StreamTokenizer.TT_EOF:
                return "End-of-Stream";
            case StreamTokenizer.TT_WORD:
                return "'" + tokenizer.sval + "'";
        }
        return "'" + (char) tokenizer.ttype + "'";
    }

    /**
     * Creates a <code>Geometry</code> using the next token in the stream.
     *
     * @return a <code>Geometry</code> specified by the next token in the stream
     * @throws ParseException if the coordinates used to create a <code>Polygon</code>
     *                        shell and holes do not form closed linestrings, or if an
     *                        unexpected token was encountered
     * @throws IOException    if an I/O error occurs
     */
    private Geometry readGeometryTaggedText() throws IOException, ParseException {
        String type;

        try {
            type = getNextWord();
        } catch (IOException | ParseException e) {
            return null;
        }

        if (POINT.equalsIgnoreCase(type)) {
            return readPointText();
        } else if (LINESTRING.equalsIgnoreCase(type)) {
            return readLineStringText();
        } else if (LINEARRING.equalsIgnoreCase(type)) {
            return readLinearRingText();
        } else if (POLYGON.equalsIgnoreCase(type)) {
            return readPolygonText();
        } else if (MULTIPOINT.equalsIgnoreCase(type)) {
            return readMultiPointText();
        } else if (MULTILINESTRING.equalsIgnoreCase(type)) {
            return readMultiLineStringText();
        } else if (MULTIPOLYGON.equalsIgnoreCase(type)) {
            return readMultiPolygonText();
        } else if (GEOMETRYCOLLECTION.equalsIgnoreCase(type)) {
            return readGeometryCollectionText();
        }
        throw new ParseException("Unknown geometry type: " + type);
    }

    /**
     * Creates a <code>Point</code> using the next token in the stream.
     *
     * @return a <code>Point</code> specified by the next token in the stream
     * @throws IOException    if an I/O error occurs
     * @throws ParseException if an unexpected token was encountered
     */
    private Point readPointText() throws IOException, ParseException {
        String nextToken = getNextEmptyOrOpener();

        if (EMPTY.equals(nextToken)) {
            return new Point((Coordinate) null);
        }

        Point point = new Point(getPreciseCoordinate());
        getNextCloser();

        return point;
    }

    /**
     * Creates a <code>LineString</code> using the next token in the stream.
     *
     * @return a <code>LineString</code> specified by the next token in the
     * stream
     * @throws IOException    if an I/O error occurs
     * @throws ParseException if an unexpected token was encountered
     */
    private LineString readLineStringText() throws IOException, ParseException {
        return new LineString(getCoordinates());
    }

    /**
     * Creates a <code>LinearRing</code> using the next token in the stream.
     *
     * @return a <code>LinearRing</code> specified by the next token in the
     * stream
     * @throws IOException    if an I/O error occurs
     * @throws ParseException if the coordinates used to create the <code>LinearRing</code>
     *                        do not form a closed linestring, or if an unexpected token
     *                        was encountered
     */
    private LinearRing readLinearRingText() throws IOException, ParseException {
        return new LinearRing(getCoordinates());
    }

    /**
     * Creates a <code>MultiPoint</code> using the next token in the stream.
     *
     * @return a <code>MultiPoint</code> specified by the next token in the
     * stream
     * @throws IOException    if an I/O error occurs
     * @throws ParseException if an unexpected token was encountered
     */
    private MultiPoint readMultiPointText() throws IOException, ParseException {
        return new MultiPoint(toPoints(getCoordinates()));
    }

    /**
     * Creates an array of <code>Point</code>s having the given
     * <code>Coordinate</code> s.
     *
     * @param coordinates the <code>Coordinate</code>s with which to create the
     *                    <code>Point</code>s
     * @return <code>Point</code>s created using this <code>WKTReader</code> s
     * <code>GeometryFactory</code>
     */
    private Point[] toPoints(Coordinate[] coordinates) {
        ArrayList<Point> points = new ArrayList<>();

        for (Coordinate coordinate : coordinates) {
            points.add(new Point(coordinate));
        }

        return points.toArray(new Point[0]);
    }

    /**
     * Creates a <code>Polygon</code> using the next token in the stream.
     *
     * @return a <code>Polygon</code> specified by the next token in the stream
     * @throws ParseException if the coordinates used to create the <code>Polygon</code>
     *                        shell and holes do not form closed linestrings, or if an
     *                        unexpected token was encountered.
     * @throws IOException    if an I/O error occurs
     */
    private Polygon readPolygonText() throws IOException, ParseException {
        String nextToken = getNextEmptyOrOpener();

        if (EMPTY.equals(nextToken)) {
            return new Polygon(new LinearRing());
        }

        LinearRing shell = readLinearRingText();
        nextToken = getNextCloserOrComma();

        ArrayList<LinearRing> holes = new ArrayList<>();
        while (COMMA.equals(nextToken)) {
            LinearRing hole = readLinearRingText();
            holes.add(hole);
            nextToken = getNextCloserOrComma();
        }

        return new Polygon(shell, holes.toArray(new LinearRing[0]));
    }

    /**
     * Creates a <code>MultiLineString</code> using the next token in the
     * stream.
     *
     * @return a <code>MultiLineString</code> specified by the next token in the
     * stream
     * @throws IOException    if an I/O error occurs
     * @throws ParseException if an unexpected token was encountered
     */
    private MultiLineString readMultiLineStringText() throws IOException, ParseException {
        String nextToken = getNextEmptyOrOpener();

        if (EMPTY.equals(nextToken)) {
            return new MultiLineString(new LineString[]{});
        }

        ArrayList<LineString> lineStrings = new ArrayList<>();
        LineString lineString = readLineStringText();
        lineStrings.add(lineString);
        nextToken = getNextCloserOrComma();

        while (COMMA.equals(nextToken)) {
            lineString = readLineStringText();
            lineStrings.add(lineString);
            nextToken = getNextCloserOrComma();
        }

        return new MultiLineString(lineStrings.toArray(new LineString[0]));
    }

    /**
     * Creates a <code>MultiPolygon</code> using the next token in the stream.
     *
     * @return a <code>MultiPolygon</code> specified by the next token in the
     * stream, or if the coordinates used to create the
     * <code>Polygon</code> shells and holes do not form closed
     * linestrings.
     * @throws IOException    if an I/O error occurs
     * @throws ParseException if an unexpected token was encountered
     */
    private MultiPolygon readMultiPolygonText() throws IOException, ParseException {
        String nextToken = getNextEmptyOrOpener();

        if (EMPTY.equals(nextToken)) {
            return new MultiPolygon(new Polygon[]{});
        }

        ArrayList<Polygon> polygons = new ArrayList<>();
        Polygon polygon = readPolygonText();
        polygons.add(polygon);
        nextToken = getNextCloserOrComma();

        while (COMMA.equals(nextToken)) {
            polygon = readPolygonText();
            polygons.add(polygon);
            nextToken = getNextCloserOrComma();
        }

        return new MultiPolygon(polygons.toArray(new Polygon[0]));
    }

    /**
     * Creates a <code>GeometryCollection</code> using the next token in the
     * stream.
     *
     * @return a <code>GeometryCollection</code> specified by the next token in
     * the stream
     * @throws ParseException if the coordinates used to create a <code>Polygon</code>
     *                        shell and holes do not form closed linestrings, or if an
     *                        unexpected token was encountered
     * @throws IOException    if an I/O error occurs
     */
    private GeometryCollection readGeometryCollectionText() throws IOException, ParseException {
        String nextToken = getNextEmptyOrOpener();

        if (EMPTY.equals(nextToken)) {
            return new GeometryCollection(new Geometry[]{});
        }

        ArrayList<Geometry> geometries = new ArrayList<>();
        Geometry geometry = readGeometryTaggedText();
        geometries.add(geometry);
        nextToken = getNextCloserOrComma();

        while (COMMA.equals(nextToken)) {
            geometry = readGeometryTaggedText();
            geometries.add(geometry);
            nextToken = getNextCloserOrComma();
        }

        return new GeometryCollection(geometries.toArray(new Geometry[0]));
    }

}
