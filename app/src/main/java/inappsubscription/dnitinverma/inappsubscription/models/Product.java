package inappsubscription.dnitinverma.inappsubscription.models;

import java.io.Serializable;

/*
* This Model is used for set and get the details of new available product details.
* */
public class Product implements Serializable{

	private String title="";
	private String price="";
	private String type="";
	private String description="";
	private String price_amount_micros="";
	private String price_currency_code="";
	private String productId="";
	public Boolean isSelect =false;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPrice_amount_micros() {
		return price_amount_micros;
	}
	public void setPrice_amount_micros(String price_amount_micros) {
		this.price_amount_micros = price_amount_micros;
	}
	public String getPrice_currency_code() {
		return price_currency_code;
	}
	public void setPrice_currency_code(String price_currency_code) {
		this.price_currency_code = price_currency_code;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	
      
}
