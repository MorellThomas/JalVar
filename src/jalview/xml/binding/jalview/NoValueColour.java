//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.05.13 at 06:58:41 PM BST 
//

package jalview.xml.binding.jalview;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for NoValueColour.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="NoValueColour">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="Min"/>
 *     &lt;enumeration value="Max"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "NoValueColour", namespace = "www.jalview.org/colours")
@XmlEnum
public enum NoValueColour
{

  @XmlEnumValue("None")
  NONE("None"), @XmlEnumValue("Min")
  MIN("Min"), @XmlEnumValue("Max")
  MAX("Max");

  private final String value;

  NoValueColour(String v)
  {
    value = v;
  }

  public String value()
  {
    return value;
  }

  public static NoValueColour fromValue(String v)
  {
    for (NoValueColour c : NoValueColour.values())
    {
      if (c.value.equals(v))
      {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }

}
