//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.05.13 at 06:58:41 PM BST 
//

package jalview.xml.binding.jalview;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for AnnotationColourScheme complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="AnnotationColourScheme">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="aboveThreshold" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="annotation" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="minColour" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="maxColour" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="colourScheme" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="threshold" type="{http://www.w3.org/2001/XMLSchema}float" />
 *       &lt;attribute name="perSequence" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="predefinedColours" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnnotationColourScheme", namespace = "www.jalview.org")
public class AnnotationColourScheme
{

  @XmlAttribute(name = "aboveThreshold")
  protected Integer aboveThreshold;

  @XmlAttribute(name = "annotation")
  protected String annotation;

  @XmlAttribute(name = "minColour")
  protected Integer minColour;

  @XmlAttribute(name = "maxColour")
  protected Integer maxColour;

  @XmlAttribute(name = "colourScheme")
  protected String colourScheme;

  @XmlAttribute(name = "threshold")
  protected Float threshold;

  @XmlAttribute(name = "perSequence")
  protected Boolean perSequence;

  @XmlAttribute(name = "predefinedColours")
  protected Boolean predefinedColours;

  /**
   * Gets the value of the aboveThreshold property.
   * 
   * @return possible object is {@link Integer }
   * 
   */
  public Integer getAboveThreshold()
  {
    return aboveThreshold;
  }

  /**
   * Sets the value of the aboveThreshold property.
   * 
   * @param value
   *          allowed object is {@link Integer }
   * 
   */
  public void setAboveThreshold(Integer value)
  {
    this.aboveThreshold = value;
  }

  /**
   * Gets the value of the annotation property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getAnnotation()
  {
    return annotation;
  }

  /**
   * Sets the value of the annotation property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setAnnotation(String value)
  {
    this.annotation = value;
  }

  /**
   * Gets the value of the minColour property.
   * 
   * @return possible object is {@link Integer }
   * 
   */
  public Integer getMinColour()
  {
    return minColour;
  }

  /**
   * Sets the value of the minColour property.
   * 
   * @param value
   *          allowed object is {@link Integer }
   * 
   */
  public void setMinColour(Integer value)
  {
    this.minColour = value;
  }

  /**
   * Gets the value of the maxColour property.
   * 
   * @return possible object is {@link Integer }
   * 
   */
  public Integer getMaxColour()
  {
    return maxColour;
  }

  /**
   * Sets the value of the maxColour property.
   * 
   * @param value
   *          allowed object is {@link Integer }
   * 
   */
  public void setMaxColour(Integer value)
  {
    this.maxColour = value;
  }

  /**
   * Gets the value of the colourScheme property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getColourScheme()
  {
    return colourScheme;
  }

  /**
   * Sets the value of the colourScheme property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setColourScheme(String value)
  {
    this.colourScheme = value;
  }

  /**
   * Gets the value of the threshold property.
   * 
   * @return possible object is {@link Float }
   * 
   */
  public Float getThreshold()
  {
    return threshold;
  }

  /**
   * Sets the value of the threshold property.
   * 
   * @param value
   *          allowed object is {@link Float }
   * 
   */
  public void setThreshold(Float value)
  {
    this.threshold = value;
  }

  /**
   * Gets the value of the perSequence property.
   * 
   * @return possible object is {@link Boolean }
   * 
   */
  public Boolean isPerSequence()
  {
    return perSequence;
  }

  /**
   * Sets the value of the perSequence property.
   * 
   * @param value
   *          allowed object is {@link Boolean }
   * 
   */
  public void setPerSequence(Boolean value)
  {
    this.perSequence = value;
  }

  /**
   * Gets the value of the predefinedColours property.
   * 
   * @return possible object is {@link Boolean }
   * 
   */
  public Boolean isPredefinedColours()
  {
    return predefinedColours;
  }

  /**
   * Sets the value of the predefinedColours property.
   * 
   * @param value
   *          allowed object is {@link Boolean }
   * 
   */
  public void setPredefinedColours(Boolean value)
  {
    this.predefinedColours = value;
  }

}
