/*
 * Copyright 2008-2009 the original author or authors.
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
package org.broadleafcommerce.core.catalog.domain.sandbox;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.catalog.domain.SkuAttribute;
import org.broadleafcommerce.core.catalog.domain.common.EmbeddedSandBoxItem;
import org.broadleafcommerce.core.catalog.domain.common.SandBoxItem;
import org.broadleafcommerce.core.catalog.service.dynamic.DefaultDynamicSkuPricingInvocationHandler;
import org.broadleafcommerce.core.catalog.service.dynamic.DynamicSkuPrices;
import org.broadleafcommerce.core.catalog.service.dynamic.SkuPricingConsiderationContext;
import org.broadleafcommerce.core.media.domain.Media;
import org.broadleafcommerce.core.media.domain.sandbox.SandBoxMediaImpl;
import org.broadleafcommerce.money.Money;
import org.broadleafcommerce.openadmin.client.presentation.SupportedFieldType;
import org.broadleafcommerce.presentation.AdminPresentation;
import org.broadleafcommerce.profile.util.DateUtil;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.MapKey;
import org.hibernate.annotations.Table;

/*
 * TODO emit these java files and compile at runtime based on an annotation
 * present in the normal entity. This will be part of special persistence
 * class handling that will be introduced into MergePersistenceUnitManager.
 */
/**
 * @author jfischer
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(appliesTo="BLC_SKU_SNDBX", indexes={
		@Index(name="SKU_SNDBX_VER_INDX", columnNames={"VERSION"}),
		@Index(name="SKU_SNDBX_NAME_INDX", columnNames={"NAME"}),
		@Index(name="SKU_SNDBX_TXBL_INDX", columnNames={"TAXABLE_FLAG"}),
		@Index(name="SKU_SNDBX_DSCNTBL_INDX", columnNames={"DISCOUNTABLE_FLAG"}),
		@Index(name="SKU_SNDBX_AVAIL_INDX", columnNames={"AVAILABLE_FLAG"}),
		@Index(name="SKU_SNDBX_ACTIVE_INDX", columnNames={"ACTIVE_START_DATE","ACTIVE_END_DATE"})
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
@Searchable
public class SandBoxSkuImpl implements Sku, SandBoxItem {
	
	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(SandBoxSkuImpl.class);

    /** The id. */
    @Id
    @GeneratedValue(generator = "SkuId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "SkuId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "SandBoxSkuImpl", allocationSize = 50)
    @Column(name = "SKU_ID")
    @SearchableId
    @AdminPresentation(friendlyName="Sku ID", group="Primary Key", hidden=true)
    protected Long id;

    /** The sale price. */
    @Column(name = "SALE_PRICE")
    @AdminPresentation(friendlyName="Sku Sale Price", order=9, group="Price", prominent=true, fieldType=SupportedFieldType.MONEY, groupOrder=3)
    protected BigDecimal salePrice;

    /** The retail price. */
    @Column(name = "RETAIL_PRICE", nullable=false)
    @AdminPresentation(friendlyName="Sku Retail Price", order=10, group="Price", prominent=true, fieldType=SupportedFieldType.MONEY, groupOrder=3)
    protected BigDecimal retailPrice;

    /** The name. */
    @Column(name = "NAME", nullable=false)
    @SearchableProperty
    @AdminPresentation(friendlyName="Sku Name", order=1, group="Sku Description", prominent=true, columnWidth="25%", groupOrder=4)
    protected String name;

    /** The description. */
    @Column(name = "DESCRIPTION")
    @AdminPresentation(friendlyName="Sku Description", order=2, group="Sku Description", largeEntry=true, groupOrder=4)
    protected String description;

    /** The long description. */
    @Column(name = "LONG_DESCRIPTION")
    @AdminPresentation(friendlyName="Sku Large Description", order=3, group="Sku Description", largeEntry=true, groupOrder=4)
    protected String longDescription;

    /** The taxable. */
    @Column(name = "TAXABLE_FLAG")
    @AdminPresentation(friendlyName="Sku Taxable", order=4, group="Sku Description", groupOrder=4)
    protected Character taxable;

    /** The discountable. */
    @Column(name = "DISCOUNTABLE_FLAG")
    @AdminPresentation(friendlyName="Sku Discountable", order=5, group="Sku Description", groupOrder=4)
    protected Character discountable;

    /** The available. */
    @Column(name = "AVAILABLE_FLAG")
    @AdminPresentation(friendlyName="Sku Available", order=6, group="Sku Description", groupOrder=4)
    protected Character available;

    /** The active start date. */
    @Column(name = "ACTIVE_START_DATE")
    @AdminPresentation(friendlyName="Sku Start Date", order=7, group="Sku Description", groupOrder=4)
    protected Date activeStartDate;

    /** The active end date. */
    @Column(name = "ACTIVE_END_DATE")
    @AdminPresentation(friendlyName="Sku End Date", order=8, group="Sku Description", groupOrder=4)
    protected Date activeEndDate;
    
    @Transient
    protected DynamicSkuPrices dynamicPrices = null;
	
    /** The sku images. */
    @CollectionOfElements
    @JoinTable(name = "BLC_SKU_SNDBX_IMAGE", joinColumns = @JoinColumn(name = "SKU_ID"))
    @org.hibernate.annotations.MapKey(columns = { @Column(name = "NAME", length = 5, nullable = false) })
    @Column(name = "URL")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
    @Deprecated
    protected Map<String, String> skuImages = new HashMap<String, String>();

    /** The sku media. */
    @ManyToMany(targetEntity = SandBoxMediaImpl.class)
    @JoinTable(name = "BLC_SKU_MEDIA_SNDBX_MAP", inverseJoinColumns = @JoinColumn(name = "MEDIA_ID", referencedColumnName = "MEDIA_ID"))
    @MapKey(columns = {@Column(name = "MAP_KEY", nullable = false)})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
    protected Map<String, Media> skuMedia = new HashMap<String , Media>();

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = SandBoxProductImpl.class)
    @JoinTable(name = "BLC_PRDCT_SKU_SNDBX_XREF", joinColumns = @JoinColumn(name = "SKU_ID", referencedColumnName = "SKU_ID", nullable = true), inverseJoinColumns = @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "PRODUCT_ID", nullable = true))
    protected List<Product> allParentProducts = new ArrayList<Product>();

    @OneToMany(mappedBy = "sku", targetEntity = SandBoxSkuAttributeImpl.class, cascade = {CascadeType.ALL})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})    
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
    @BatchSize(size = 50)
    protected List<SkuAttribute> skuAttributes  = new ArrayList<SkuAttribute>();
    
    @Embedded
    protected SandBoxItem sandBoxItem = new EmbeddedSandBoxItem();
    
    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#getId()
     */
    public Long getId() {
        return id;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#setId(java.lang.Long)
     */
    public void setId(Long id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#getSalePrice()
     */
    public Money getSalePrice() {
    	if (dynamicPrices != null) {
    		return dynamicPrices.getSalePrice();
    	}
    	if (
    			SkuPricingConsiderationContext.getSkuPricingConsiderationContext() != null && 
    			SkuPricingConsiderationContext.getSkuPricingConsiderationContext().size() > 0 &&
    			SkuPricingConsiderationContext.getSkuPricingService() != null
    	) {
    		DefaultDynamicSkuPricingInvocationHandler handler = new DefaultDynamicSkuPricingInvocationHandler(this);
    		Sku proxy = (Sku) Proxy.newProxyInstance(getClass().getClassLoader(), getClass().getInterfaces(), handler);
    		dynamicPrices = SkuPricingConsiderationContext.getSkuPricingService().getSkuPrices(proxy, SkuPricingConsiderationContext.getSkuPricingConsiderationContext());
    		handler.reset();
    		return dynamicPrices.getSalePrice();
    	}
        return salePrice == null ? null : new Money(salePrice);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.core.catalog.domain.Sku#setSalePrice(org.broadleafcommerce.util.money.Money)
     */
    public void setSalePrice(Money salePrice) {
        this.salePrice = Money.toAmount(salePrice);
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#getRetailPrice()
     */
    public Money getRetailPrice() {
    	if (dynamicPrices != null) {
    		return dynamicPrices.getRetailPrice();
    	}
    	if (
    			SkuPricingConsiderationContext.getSkuPricingConsiderationContext() != null && 
    			SkuPricingConsiderationContext.getSkuPricingConsiderationContext().size() > 0 &&
    			SkuPricingConsiderationContext.getSkuPricingService() != null
    	) {
    		DefaultDynamicSkuPricingInvocationHandler handler = new DefaultDynamicSkuPricingInvocationHandler(this);
    		Sku proxy = (Sku) Proxy.newProxyInstance(getClass().getClassLoader(), getClass().getInterfaces(), handler);
    		dynamicPrices = SkuPricingConsiderationContext.getSkuPricingService().getSkuPrices(proxy, SkuPricingConsiderationContext.getSkuPricingConsiderationContext());
    		handler.reset();
    		return dynamicPrices.getRetailPrice();
    	}
        return retailPrice == null ? null : new Money(retailPrice);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.core.catalog.domain.Sku#setRetailPrice(org.broadleafcommerce
     * .util.money.Money)
     */
    public void setRetailPrice(Money retailPrice) {
        this.retailPrice = Money.toAmount(retailPrice);
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#getListPrice()
     */
    public Money getListPrice() {
        return new Money(retailPrice);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.core.catalog.domain.Sku#setListPrice(org.broadleafcommerce
     * .util.money.Money)
     */
    public void setListPrice(Money listPrice) {
        this.retailPrice = Money.toAmount(listPrice);
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.core.catalog.domain.Sku#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#getLongDescription()
     */
    public String getLongDescription() {
        return longDescription;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.core.catalog.domain.Sku#setLongDescription(java.lang
     * .String)
     */
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#isTaxable()
     */
    public Boolean isTaxable() {
        if (taxable == null)
            return null;
        return taxable == 'Y' ? Boolean.TRUE : Boolean.FALSE;
    }

    /*
     * This is to facilitate serialization to non-Java clients
     */
    public Boolean getTaxable() {
        return isTaxable();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.core.catalog.domain.Sku#setTaxable(java.lang.Boolean)
     */
    public void setTaxable(Boolean taxable) {
        if (taxable == null) {
            this.taxable = null;
        } else {
            this.taxable = taxable ? 'Y' : 'N';
        }
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#isDiscountable()
     */
    public Boolean isDiscountable() {
        if (discountable == null)
            return null;
        return discountable == 'Y' ? Boolean.TRUE : Boolean.FALSE;
    }

    /*
     * This is to facilitate serialization to non-Java clients
     */
    public Boolean getDiscountable() {
        return isDiscountable();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.core.catalog.domain.Sku#setDiscountable(java.lang.Boolean)
     */
    public void setDiscountable(Boolean discountable) {
        if (discountable == null) {
            this.discountable = null;
        } else {
            this.discountable = discountable ? 'Y' : 'N';
        }
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#isAvailable()
     */
    public Boolean isAvailable() {
        if (available == null)
            return null;
        return available == 'Y' ? Boolean.TRUE : Boolean.FALSE;
    }
    
    public Boolean getAvailable() {
    	return isAvailable();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.core.catalog.domain.Sku#setAvailable(java.lang.Boolean)
     */
    public void setAvailable(Boolean available) {
        if (available == null) {
            this.available = null;
        } else {
            this.available = available ? 'Y' : 'N';
        }
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#getActiveStartDate()
     */
    public Date getActiveStartDate() {
        return activeStartDate;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.core.catalog.domain.Sku#setActiveStartDate(java.util
     * .Date)
     */
    public void setActiveStartDate(Date activeStartDate) {
        this.activeStartDate = activeStartDate;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#getActiveEndDate()
     */
    public Date getActiveEndDate() {
        return activeEndDate;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.core.catalog.domain.Sku#setActiveEndDate(java.util.Date)
     */
    public void setActiveEndDate(Date activeEndDate) {
        this.activeEndDate = activeEndDate;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#isActive()
     */
    public boolean isActive() {
        if (LOG.isDebugEnabled()) {
            if (!DateUtil.isActive(getActiveStartDate(), getActiveEndDate(), false)) {
                LOG.debug("sku, " + id + ", inactive due to date");
            }
        }
        return DateUtil.isActive(getActiveStartDate(), getActiveEndDate(), false);
    }

    public boolean isActive(Product product, Category category) {
        if (LOG.isDebugEnabled()) {
            if (!DateUtil.isActive(getActiveStartDate(), getActiveEndDate(), false)) {
                LOG.debug("sku, " + id + ", inactive due to date");
            } else if (!product.isActive()) {
                LOG.debug("sku, " + id + ", inactive due to product being inactive");
            } else if (!category.isActive()) {
                LOG.debug("sku, " + id + ", inactive due to category being inactive");
            }
        }
        return this.isActive() && product.isActive() && category.isActive();
    }
    
    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#getSkuImages()
     */
    @Deprecated
    public Map<String, String> getSkuImages() {
        return skuImages;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.core.catalog.domain.Sku#getSkuImage(java.lang.String)
     */
    @Deprecated
    public String getSkuImage(String imageKey) {
        return skuImages.get(imageKey);
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.core.catalog.domain.Sku#setSkuImages(java.util.Map)
     */
    @Deprecated
    public void setSkuImages(Map<String, String> skuImages) {
        this.skuImages = skuImages;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.core.catalog.domain.Sku#getSkuMedia()
     */
    public Map<String, Media> getSkuMedia() {
        return skuMedia;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.core.catalog.domain.Sku#getSkuImage(java.util.Map)
     */
    public void setSkuMedia(Map<String, Media> skuMedia) {
        this.skuMedia = skuMedia;
    }

    public List<Product> getAllParentProducts() {
        return allParentProducts;
    }

    public void setAllParentProducts(List<Product> allParentProducts) {
        this.allParentProducts = allParentProducts;
    }
    
    /**
	 * @return the skuAttributes
	 */
	public List<SkuAttribute> getSkuAttributes() {
		return skuAttributes;
	}

	/**
	 * @param skuAttributes the skuAttributes to set
	 */
	public void setSkuAttributes(List<SkuAttribute> skuAttributes) {
		this.skuAttributes = skuAttributes;
	}

    /**
	 * @return
	 * @see org.broadleafcommerce.core.catalog.domain.common.SandBoxItem#getVersion()
	 */
	public long getVersion() {
		return sandBoxItem.getVersion();
	}

	/**
	 * @param version
	 * @see org.broadleafcommerce.core.catalog.domain.common.SandBoxItem#setVersion(long)
	 */
	public void setVersion(long version) {
		sandBoxItem.setVersion(version);
	}

	/**
	 * @return
	 * @see org.broadleafcommerce.core.catalog.domain.common.SandBoxItem#isDirty()
	 */
	public boolean isDirty() {
		return sandBoxItem.isDirty();
	}

	/**
	 * @param dirty
	 * @see org.broadleafcommerce.core.catalog.domain.common.SandBoxItem#setDirty(boolean)
	 */
	public void setDirty(boolean dirty) {
		sandBoxItem.setDirty(dirty);
	}

	/**
	 * @return
	 * @see org.broadleafcommerce.core.catalog.domain.common.SandBoxItem#getCommaDelimitedDirtyFields()
	 */
	public String getCommaDelimitedDirtyFields() {
		return sandBoxItem.getCommaDelimitedDirtyFields();
	}

	/**
	 * @param commaDelimitedDirtyFields
	 * @see org.broadleafcommerce.core.catalog.domain.common.SandBoxItem#setCommaDelimitedDirtyFields(java.lang.String)
	 */
	public void setCommaDelimitedDirtyFields(String commaDelimitedDirtyFields) {
		sandBoxItem.setCommaDelimitedDirtyFields(commaDelimitedDirtyFields);
	}

	@Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SandBoxSkuImpl other = (SandBoxSkuImpl) obj;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
}
