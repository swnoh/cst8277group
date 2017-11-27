/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

/**
 *
 * @author http://www.thoughts-on-java.org/2013/10/jpa-21-how-to-implement-type-
converter.html
 */
import java.awt.Color;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Converter(autoApply=true)
public class ColorConverter implements AttributeConverter<Color,String> {

 private static final String SEPARATOR = "-";

 /**
  * Convert Color object to a String 
  * with format red|green|blue|alpha
     * @param color
     * @return 
  */
 @Override
 public String convertToDatabaseColumn(Color color) {
  StringBuilder sb = new StringBuilder();
  sb.append(color.getRed()).append(SEPARATOR)
     .append(color.getGreen())
     .append(SEPARATOR)
     .append(color.getBlue())
     .append(SEPARATOR)
     .append(color.getAlpha());
  //return sb.toString();
  return color.toString();
 }

 /**
  * Convert a String with format java.awt.Color[r=0,g=0,b=255]
  * to a Color object
     * @param colorString
     * @return 
  */
 @Override
 public Color convertToEntityAttribute(String colorString) {
  //String[] rgb = colorString.split(SEPARATOR);
     Pattern p = Pattern.compile("java.awt.Color\\[r=(\\d+),g=(\\d+),b=(\\d+)\\]");
     Matcher m = p.matcher(colorString);
//  return new Color(Integer.parseInt(rgb[0]), 
//      Integer.parseInt(rgb[1]),
//      Integer.parseInt(rgb[2]), 
//      Integer.parseInt(rgb[3]));
     if (m.matches())
     return new Color(Integer.parseInt(m.group(1)),
                      Integer.parseInt(m.group(2)),
                      Integer.parseInt(m.group(3)));
     else {
         System.out.println("Could not convert the input string " + colorString + " to a Color object");
         return Color.BLUE;}
 }
}
