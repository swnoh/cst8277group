/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web;

/**
 *
 * @author tgk
 */
import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("web.ColorConverter")
public class ColorConverter implements Converter{
    @Override
   public String getAsString(FacesContext facesContext,
      UIComponent component, Object value) {
         return value.toString();
   } 
       public String getRgb(Color color){
        if (color != null)
            return String.format("#%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue());
        else
            return "#0000FF";
    }
//    public void setRgb(String rgb){
//        int red = Integer.parseInt(rgb.substring(1,3),16);
//        int green = Integer.parseInt(rgb.substring(3,5),16);
//        int blue = Integer.parseInt(rgb.substring(5,7),16);
//        color = new Color(red,green,blue);
//    }


 /**
  * Convert a String with format java.awt.Color[r=0,g=0,b=255]
  * to a Color object
     * @param colorString
     * @return 
  */
 @Override
   public Object getAsObject(FacesContext facesContext, 
      UIComponent component, String value) {
  //String[] rgb = colorString.split(SEPARATOR);
     Pattern p = Pattern.compile("java.awt.Color\\[r=(\\d+),g=(\\d+),b=(\\d+)\\]");
     Matcher m = p.matcher(value);
//  return new Color(Integer.parseInt(rgb[0]), 
//      Integer.parseInt(rgb[1]),
//      Integer.parseInt(rgb[2]), 
//      Integer.parseInt(rgb[3]));
     if (m.matches())
     return new Color(Integer.parseInt(m.group(1)),
                      Integer.parseInt(m.group(2)),
                      Integer.parseInt(m.group(3)));
     else {
         System.out.println("Could not convert the input string " + value + " to a Color object");
         return Color.BLUE;}
 }
    
}
