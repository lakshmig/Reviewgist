(function($) {
  $.widget('mobile.tabbar', $.mobile.navbar, {
    _create: function() {
      // Set the theme before we call the prototype, which will 
      // ensure buttonMarkup() correctly grabs the inheritied theme.
      // We default to the "a" swatch if none is found
      var theme = this.element.jqmData('theme') || "a";
      this.element.addClass('ui-footer ui-footer-fixed ui-bar-' + theme);

      // Make sure the page has padding added to it to account for the fixed bar
      this.element.closest('[data-role="page"]').addClass('ui-page-footer-fixed');


      // Call the NavBar _create prototype
      $.mobile.navbar.prototype._create.call(this);
    },

    // Set the active URL for the Tab Bar, and highlight that button on the bar
    setActive: function(url) {
      // Sometimes the active state isn't properly cleared, so we reset it ourselves
      this.element.find('a').removeClass('ui-btn-active ui-state-persist');
      this.element.find('a[href="' + url + '"]').addClass('ui-btn-active ui-state-persist');
    }
  });

  $(document).bind('pagecreate create', function(e) {
    return $(e.target).find(":jqmData(role='tabbar')").tabbar();
  });
  
  $(":jqmData(role='page')").live('pageshow', function(e) {
    // Grab the id of the page that's showing, and select it on the Tab Bar on the page
    var tabBar, id = $(e.target).attr('id');

    tabBar = $.mobile.activePage.find(':jqmData(role="tabbar")');
    if(tabBar.length) {
      tabBar.tabbar('setActive', '#' + id);
    }
  });

var attachEvents = function() {
	var hoverDelay = $.mobile.buttonMarkup.hoverDelay, hov, foc;

	$( document ).bind( {
		"vmousedown vmousecancel vmouseup vmouseover vmouseout focus blur scrollstart": function( event ) {
			var theme,
				$btn = $( closestEnabledButton( event.target ) ),
				evt = event.type;
		
			if ( $btn.length ) {
				theme = $btn.attr( "data-" + $.mobile.ns + "theme" );
		
				if ( evt === "vmousedown" ) {
					if ( $.support.touch ) {
						hov = setTimeout(function() {
							$btn.removeClass( "ui-btn-up-" + theme ).addClass( "ui-btn-down-" + theme );
						}, hoverDelay );
					} else {
						$btn.removeClass( "ui-btn-up-" + theme ).addClass( "ui-btn-down-" + theme );
					}
				} else if ( evt === "vmousecancel" || evt === "vmouseup" ) {
					$btn.removeClass( "ui-btn-down-" + theme ).addClass( "ui-btn-up-" + theme );
				} else if ( evt === "vmouseover" || evt === "focus" ) {
					if ( $.support.touch ) {
						foc = setTimeout(function() {
							$btn.removeClass( "ui-btn-up-" + theme ).addClass( "ui-btn-hover-" + theme );
						}, hoverDelay );
					} else {
						$btn.removeClass( "ui-btn-up-" + theme ).addClass( "ui-btn-hover-" + theme );
					}
				} else if ( evt === "vmouseout" || evt === "blur" || evt === "scrollstart" ) {
					$btn.removeClass( "ui-btn-hover-" + theme  + " ui-btn-down-" + theme ).addClass( "ui-btn-up-" + theme );
					if ( hov ) {
						clearTimeout( hov );
					}
					if ( foc ) {
						clearTimeout( foc );
					}
				}
			}
		},
		"focusin focus": function( event ){
			$( closestEnabledButton( event.target ) ).addClass( $.mobile.focusClass );
		},
		"focusout blur": function( event ){
			$( closestEnabledButton( event.target ) ).removeClass( $.mobile.focusClass );
		}
	});

	attachEvents = null;
};

$.fn.buttonMarkup = function( options ) {
	var $workingSet = this;

	// Enforce options to be of type string
	options = ( options && ( $.type( options ) == "object" ) )? options : {};
	for ( var i = 0; i < $workingSet.length; i++ ) {
		var el = $workingSet.eq( i ),
			e = el[ 0 ],
			o = $.extend( {}, $.fn.buttonMarkup.defaults, {
				icon:       options.icon       !== undefined ? options.icon       : el.jqmData( "icon" ),
				iconpos:    options.iconpos    !== undefined ? options.iconpos    : el.jqmData( "iconpos" ),
				theme:      options.theme      !== undefined ? options.theme      : el.jqmData( "theme" ) || $.mobile.getInheritedTheme( el, "c" ),
				inline:     options.inline     !== undefined ? options.inline     : el.jqmData( "inline" ),
				shadow:     options.shadow     !== undefined ? options.shadow     : el.jqmData( "shadow" ),
				corners:    options.corners    !== undefined ? options.corners    : el.jqmData( "corners" ),
				iconshadow: options.iconshadow !== undefined ? options.iconshadow : el.jqmData( "iconshadow" ),
				iconsize:   options.iconsize   !== undefined ? options.iconsize   : el.jqmData( "iconsize" ),
				mini:       options.mini       !== undefined ? options.mini       : el.jqmData( "mini" )
			}, options ),

			// Classes Defined
			innerClass = "ui-btn-inner",
			textClass = "ui-btn-text",
			buttonClass, iconClass,
			// Button inner markup
			buttonInner,
			buttonText,
			buttonIcon,
			buttonElements;

		$.each(o, function(key, value) {
			e.setAttribute( "data-" + $.mobile.ns + key, value );
			el.jqmData(key, value);
		});

		// Check if this element is already enhanced
		buttonElements = $.data(((e.tagName === "INPUT" || e.tagName === "BUTTON") ? e.parentNode : e), "buttonElements");

		if (buttonElements) {
			e = buttonElements.outer;
			el = $(e);
			buttonInner = buttonElements.inner;
			buttonText = buttonElements.text;
			// We will recreate this icon below
			$(buttonElements.icon).remove();
			buttonElements.icon = null;
		}
		else {
			buttonInner = document.createElement( o.wrapperEls );
			buttonText = document.createElement( o.wrapperEls );
		}
		buttonIcon = o.icon ? document.createElement( "span" ) : null;

		if ( attachEvents && !buttonElements) {
			attachEvents();
		}
		
		// if not, try to find closest theme container	
		if ( !o.theme ) {
			o.theme = $.mobile.getInheritedTheme( el, "c" );	
		}		

		buttonClass = "ui-btn ui-btn-up-" + o.theme;
		buttonClass += o.inline ? " ui-btn-inline" : "";
		buttonClass += o.shadow ? " ui-shadow" : "";
		buttonClass += o.corners ? " ui-btn-corner-all" : "";

		if ( o.mini !== undefined ) {
			// Used to control styling in headers/footers, where buttons default to `mini` style.
			buttonClass += o.mini ? " ui-mini" : " ui-fullsize";
		}
		
		if ( o.inline !== undefined ) {			
			// Used to control styling in headers/footers, where buttons default to `mini` style.
			buttonClass += o.inline === false ? " ui-btn-block" : " ui-btn-inline";
		}
		
		
		if ( o.icon ) {
			o.icon = "ui-icon-" + o.icon;
			o.iconpos = o.iconpos || "left";

			iconClass = "ui-icon " + o.icon;

			if ( o.iconshadow ) {
				iconClass += " ui-icon-shadow";
			}

			if ( o.iconsize ) {
				iconClass += " ui-iconsize-" + o.iconsize;
			}
		}

		if ( o.iconpos ) {
			buttonClass += " ui-btn-icon-" + o.iconpos;

			if ( o.iconpos == "notext" && !el.attr( "title" ) ) {
				el.attr( "title", el.getEncodedText() );
			}
		}
    
		innerClass += o.corners ? " ui-btn-corner-all" : "";

		if ( o.iconpos && o.iconpos === "notext" && !el.attr( "title" ) ) {
			el.attr( "title", el.getEncodedText() );
		}

		if ( buttonElements ) {
			el.removeClass( buttonElements.bcls || "" );
		}
		el.removeClass( "ui-link" ).addClass( buttonClass );

		buttonInner.className = innerClass;

		buttonText.className = textClass;
		if ( !buttonElements ) {
			buttonInner.appendChild( buttonText );
		}
		if ( buttonIcon ) {
			buttonIcon.className = iconClass;
			if ( !(buttonElements && buttonElements.icon) ) {
				buttonIcon.appendChild( document.createTextNode("\u00a0") );
				buttonInner.appendChild( buttonIcon );
			}
		}

		while ( e.firstChild && !buttonElements) {
			buttonText.appendChild( e.firstChild );
		}

		if ( !buttonElements ) {
			e.appendChild( buttonInner );
		}

		// Assign a structure containing the elements of this button to the elements of this button. This
		// will allow us to recognize this as an already-enhanced button in future calls to buttonMarkup().
		buttonElements = {
			bcls  : buttonClass,
			outer : e,
			inner : buttonInner,
			text  : buttonText,
			icon  : buttonIcon
		};

		$.data(e,           'buttonElements', buttonElements);
		$.data(buttonInner, 'buttonElements', buttonElements);
		$.data(buttonText,  'buttonElements', buttonElements);
		if (buttonIcon) {
			$.data(buttonIcon, 'buttonElements', buttonElements);
		}
	}

	return this;
};

$.fn.buttonMarkup.defaults = {
	corners: true,
	shadow: true,
	iconshadow: true,
	iconsize: 18,
	wrapperEls: "span"
};

function closestEnabledButton( element ) {
    var cname;

    while ( element ) {
		// Note that we check for typeof className below because the element we
		// handed could be in an SVG DOM where className on SVG elements is defined to
		// be of a different type (SVGAnimatedString). We only operate on HTML DOM
		// elements, so we look for plain "string".
        cname = ( typeof element.className === 'string' ) && (element.className + ' ');
        if ( cname && cname.indexOf("ui-btn ") > -1 && cname.indexOf("ui-disabled ") < 0 ) {
            break;
        }

        element = element.parentNode;
    }

    return element;
}

$(document).ready(function () {
	 document.addEventListener("deviceready", startApp, false);
	$.getJSON("http://www.reviewgist.de/api?operation=listmodels&format=json",
	 function(data) {
				 $.each(data.response.models, function(i,model){
					 $('#listmodels').append('<li data-theme="c" id='+ model.model_id + '><a href="#page4" data-transition="slide" onclick="getBrands(\'' + model.model_id +'\',\''+ model.display_name + '\')">' + model.display_name + '</a></li>');
				 });
				 $('#listmodels').listview('refresh');
			 }
	 );
  }); 
})(jQuery);

var fullReviewUrl = "";
/**
 * Start the App
 */
function startApp() {
	//alert("in device ready");
   // childBrowser = new ChildBrowser();
};

function getBrands(Id,display_name){	
	$('#brands').html(display_name);
	//alert("in get brands");
	$.getJSON("http://www.reviewgist.de/api?operation=listbrands&model_id="+Id+"&format=json",
			 function(data) {	
						//alert("in getbrands function data");
						$('#listbrands li').remove();
						//alert("in getbrands function after removing li");
						 $.each(data.response.brands, function(i,brand){
							// alert("in getbrands function inside each before appending");
							 $('#listbrands').append('<li data-theme="c" id='+ brand.brand_id + '><a href="#page5" data-transition="slide" onclick="getListing(\'' + Id +'\',\''+ brand.brand_id +'\',\''+ brand.display_name + '\')">' + brand.display_name + '</a></li>');
							// alert("in getbrands function inside each after apending");
						 });
						 $('#listbrands').listview('refresh');
						 //alert("in getbrands function  after refresh");
					 });
	
};

function getListing(model_id,brand_id,display_name){
	//alert("in getListing");
	var count = 10;
	$('#listing').html(display_name);	
	pupulateListing(model_id,brand_id,display_name,count);
	//alert("after populateListing");
	$('#loadmore').bind('click', function () {
		count += 10;
		//alert("after loadmore " +count);
		pupulateListing(model_id,brand_id,display_name,count);		
		});
};

function getProduct(pName,imgUrl,pPrice,config_id){
	//alert("in getProduct");
	$('#product').html(pName);
	if(imgUrl)
		{
		$('#product_img').attr('src',imgUrl);
		}
	if(pPrice)
		{
		$('#product_price_button').html(pPrice);
		}
	$.getJSON("http://www.reviewgist.de/api?operation=productsummary&config_id="+config_id+"&format=json",
			 function(data) {
				//alert("in getProduct function data");
				$('#product_reviews li').remove();
						 $.each(data.response.reviews, function(i,review){
							 var stardiv = '<div id="stardiv" class="reviewRating span2">'; 
							 var int = review.star_rating.substr(0,1);
						     var decimal = review.star_rating.substr(1,review.star_rating.length);
							 for(var i =0;i<int;i++)
							 	{
								 stardiv += '<img src="./img/star.png" width="16" height="16" style="margin-right:2px;">'							 
							 	}
							 if(decimal !=0 && decimal <= .5)
							 	{
								 stardiv += '<img src="./img/halfstar.png" width="16" height="16" style="margin-right:2px;">'	
								 }
							 else if(decimal > .5)
							 	{
								 stardiv += '<img src="./img/star.png" width="16" height="16" style="margin-right:2px;">'	 
							 	}
							 stardiv += '</div>'
							var li = '<li data-theme="c"><a href="#page8" data-transition="slide" onclick="getReview(\'' + config_id +'\',\''+ review.name +'\',\''+ pName + '\')">' + stardiv + review.name + '</a></li>';
							 $('#product_reviews').append(li);
						 });
						 $('#product_reviews').listview('refresh');
					 });
	
};	

function getReview(configId,rName,pName){
	//alert("in getReview function data");
	$.getJSON("http://www.reviewgist.de/api?operation=productsummary&config_id="+configId+"&format=json",
			 function(data) {
				//alert("in getReview function data");
						 $.each(data.response.reviews, function(i,review){
							if(review.name == rName)
								{
								$('#review_product').html(pName);
								$('#review_prd_img').attr('src',data.response.image_url);
								$('#reviewRating img').remove();
								 var int = review.star_rating.substr(0,1);
							     var decimal = review.star_rating.substr(1,review.star_rating.length);
								 for(var i = 0;i < int; i++){
									$('#reviewRating').append('<img src="./img/star.png" width="16" height="16" style="margin-right:2px;">');		
									}
								 if(decimal !=0 && decimal <= .5)
								 	{
									 $('#reviewRating').append('<img src="./img/halfstar.png" width="16" height="16" style="margin-right:2px;">');		
									 }
								 else if(decimal > .5)
								 	{
									 $('#reviewRating').append('<img src="./img/star.png" width="16" height="16" style="margin-right:2px;">');		 
								 	}
								$('#review_rating_name').html(review.name);
								$('#date_span').html(review.date);
								$('#summery').html(review.summary);
								//$('#frame').attr('src',review.url); 
								fullReviewUrl = review.url;
								//alert(review.url);
								}
						 });
					 });	
};

function pupulateListing(model_id,brand_id,display_name,count){
	//alert("in pupulateListing");
	$.getJSON("http://www.reviewgist.de/api?operation=search&model_id="+model_id+"&brand_id="+brand_id+"&pageitems="+count+"&format=json",
			 function(data) {
					//alert("in pupulateListing function data");
						 $('#productlists li').remove();
						 $.each(data.response.products, function(i,product){
							 //removng the brandname from the product.name as it becomes leanghty and the models are not visible
							var pName = product.name;
							 pName = pName.substring(display_name.length, pName.length);
							 if(product.best_price)
								 {
								 $('#productlists').append('<li data-theme="c"><a href="#page6" data-transition="slide" onclick="getProduct(\'' + pName +'\',\'' + product.image_url + '\',\'' + product.best_price + '\',\''+ product.config_id + '\')">' + pName +'<span class="ui-li-count">' + product.best_price + '</span></a></li>');
								 }
							 else
								 {
								 $('#productlists').append('<li data-theme="c"><a href="#page6" data-transition="slide" onclick="getProduct(\'' + pName +'\',\'' + product.image_url + '\',\'' + product.best_price + '\',\''+ product.config_id + '\')">' + pName + '</a></li>');
								 }
						 });
						 $('#productlists').listview('refresh');
					 });	
};

function loadReviewPage(){
	//alert("in loadReviewPage " + fullReviewUrl);
	// Now open new browser
	window.plugins.childBrowser.showWebPage(fullReviewUrl, {showLocationBar : true}); 	
}
