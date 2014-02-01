package at.workflow.webdesk.po.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;

import at.workflow.webdesk.po.ImageAware;

/**
 * This is extension of PoPerson class 
 * containing all the person related image information.
 * 
 * @author sdzuban 18.02.2013
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class PoPersonImages extends PoBase implements ImageAware {
	
	@Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name="PERSONIMAGES_UID", length=32)
	private String uid;

	@OneToOne (fetch=FetchType.LAZY)
	@JoinColumn (name="ORIGINAL_UID", nullable = false)
	@ForeignKey (name="FK_PERSONIMAGES_ORIGINAL")
	private PoImage original;
	
	@OneToOne (fetch=FetchType.EAGER)
	@JoinColumn (name="IMAGE_UID", nullable = false)
	@ForeignKey (name="FK_PERSONIMAGES_IMAGE")
	private PoImage image;
	
	@OneToOne (fetch=FetchType.EAGER)
	@JoinColumn (name="THUMBNAIL_UID", nullable = false)
	@ForeignKey (name="FK_PERSONIMAGES_THUMBNAIL")
	private PoImage thumbnail;
	
	@Column(nullable=false)
	private int cropStartX;
	
	@Column(nullable=false)
	private int cropStartY;
	
	@Column(nullable=false)
	private int cropWidth;
	
	@Column(nullable=false)
	private int cropHeight;

    @Override
	public String getUID() {
		return uid;
	}

    @Override
	public void setUID(String uid) {
		this.uid = uid;
	}

	@Override
	public PoImage getOriginal() {
		return original;
	}

	@Override
	public PoImage getImage() {
		return image;
	}

	@Override
	public PoImage getThumbnail() {
		return thumbnail;
	}

	@Override
	public void setOriginal(PoImage original) {
		this.original = original;
	}

	@Override
	public void setImage(PoImage image) {
		this.image = image;
	}

	@Override
	public void setThumbnail(PoImage thumbnail) {
		this.thumbnail = thumbnail;
	}

	@Override
	public int getCropStartX() {
		return cropStartX;
	}

	@Override
	public int getCropStartY() {
		return cropStartY;
	}

	@Override
	public int getCropWidth() {
		return cropWidth;
	}

	@Override
	public int getCropHeight() {
		return cropHeight;
	}

	@Override
	public void setCropStartX(int x) {
		this.cropStartX = x;
	}

	@Override
	public void setCropStartY(int y) {
		this.cropStartY = y;
	}

	@Override
	public void setCropWidth(int width) {
		this.cropWidth = width;
	}

	@Override
	public void setCropHeight(int height) {
		this.cropHeight = height;
	}

}