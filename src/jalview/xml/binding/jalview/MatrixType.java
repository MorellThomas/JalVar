//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.05.13 at 06:58:41 PM BST 
//


package jalview.xml.binding.jalview;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>
 * Java class for MatrixType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="MatrixType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="elements" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="groups" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="newick" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="property" type="{www.vamsas.ac.uk/jalview/version2}property" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mapping" type="{www.vamsas.ac.uk/jalview/version2}mapListType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="rows" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="cols" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="treeMethod" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="cutHeight" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "MatrixType",
  propOrder =
  { "elements", "groups", "newick", "property", "mapping" })
public class MatrixType
{

  @XmlElement(required = true)
  protected String elements;

  protected List<String> groups;

  protected List<String> newick;

  protected List<Property> property;

  protected MapListType mapping;

  @XmlAttribute(name = "type", required = true)
  protected String type;

  @XmlAttribute(name = "rows", required = true)
  protected BigInteger rows;

  @XmlAttribute(name = "cols", required = true)
  protected BigInteger cols;

  @XmlAttribute(name = "treeMethod")
  protected String treeMethod;

  @XmlAttribute(name = "cutHeight")
  protected Double cutHeight;

  @XmlAttribute(name = "id")
  protected String id;

  /**
   * Gets the value of the elements property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getElements()
  {
    return elements;
  }

  /**
   * Sets the value of the elements property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setElements(String value)
  {
    this.elements = value;
  }

  /**
   * Gets the value of the groups property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot.
   * Therefore any modification you make to the returned list will be present
   * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
   * for the groups property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getGroups().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link String }
   * 
   * 
   */
  public List<String> getGroups()
  {
    if (groups == null)
    {
      groups = new ArrayList<String>();
    }
    return this.groups;
  }

  /**
   * Gets the value of the newick property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot.
   * Therefore any modification you make to the returned list will be present
   * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
   * for the newick property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getNewick().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link String }
   * 
   * 
   */
  public List<String> getNewick()
  {
    if (newick == null)
    {
      newick = new ArrayList<String>();
    }
    return this.newick;
  }

  /**
   * Gets the value of the property property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot.
   * Therefore any modification you make to the returned list will be present
   * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
   * for the property property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getProperty().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link Property }
   * 
   * 
   */
  public List<Property> getProperty()
  {
    if (property == null)
    {
      property = new ArrayList<Property>();
    }
    return this.property;
  }

  /**
   * Gets the value of the mapping property.
   * 
   * @return possible object is {@link MapListType }
   * 
   */
  public MapListType getMapping()
  {
    return mapping;
  }

  /**
   * Sets the value of the mapping property.
   * 
   * @param value
   *          allowed object is {@link MapListType }
   * 
   */
  public void setMapping(MapListType value)
  {
    this.mapping = value;
  }

  /**
   * Gets the value of the type property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getType()
  {
    return type;
  }

  /**
   * Sets the value of the type property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setType(String value)
  {
    this.type = value;
  }

  /**
   * Gets the value of the rows property.
   * 
   * @return possible object is {@link BigInteger }
   * 
   */
  public BigInteger getRows()
  {
    return rows;
  }

  /**
   * Sets the value of the rows property.
   * 
   * @param value
   *          allowed object is {@link BigInteger }
   * 
   */
  public void setRows(BigInteger value)
  {
    this.rows = value;
  }

  /**
   * Gets the value of the cols property.
   * 
   * @return possible object is {@link BigInteger }
   * 
   */
  public BigInteger getCols()
  {
    return cols;
  }

  /**
   * Sets the value of the cols property.
   * 
   * @param value
   *          allowed object is {@link BigInteger }
   * 
   */
  public void setCols(BigInteger value)
  {
    this.cols = value;
  }

  /**
   * Gets the value of the treeMethod property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getTreeMethod()
  {
    return treeMethod;
  }

  /**
   * Sets the value of the treeMethod property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setTreeMethod(String value)
  {
    this.treeMethod = value;
  }

  /**
   * Gets the value of the cutHeight property.
   * 
   * @return possible object is {@link Double }
   * 
   */
  public Double getCutHeight()
  {
    return cutHeight;
  }

  /**
   * Sets the value of the cutHeight property.
   * 
   * @param value
   *          allowed object is {@link Double }
   * 
   */
  public void setCutHeight(Double value)
  {
    this.cutHeight = value;
  }

  /**
   * Gets the value of the id property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getId()
  {
    return id;
  }

  /**
   * Sets the value of the id property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setId(String value)
  {
    this.id = value;
  }

}
