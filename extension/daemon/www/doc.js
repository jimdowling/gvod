/*
    Written by Riccardo Petrocco 
    based on "Documentation Template" 
    example from css-tricks.com 
*/

$(function() {
    
    var $el = $(),
        $mainContent = $("#main-content");

    // Used for fading out the content while leaving whiteness/main content area along     
    $mainContent.wrapInner("<div id='fade-wrapper' />");
    
    // add in AJAX spinning graphic (hidden by CSS)
    $mainContent.append('<img src="images/ajax-loader.gif" id="ajax-loader" />');        
    
    setButton();
    
    var $fadeWrapper = $("#fade-wrapper"),
        $allNav = $("#main-nav a"),
        $allListItems = $("#main-nav li"),
        url = '',
        liClass = '',
        hash = window.location.hash,
        $ajaxLoader = $("#ajax-loader");
    
    // remove ID, which is used only for nav highlighting in non-JS version            
    $("body").attr("id", "");    
    
    // If, when the page loads, it has a #hash value in the URL
    if (hash) {
        hash = hash.substring(1);
        liClass = hash.substring(0,hash.length-4);
        url = hash + " #inside-content";
        $fadeWrapper.load(url);
        $("." + liClass).addClass("active");
    } else {
        // No hash tag present, so make the first item in the nav the active nav
        $("#main-nav li:first").addClass("active");
    }
        
    $allNav.click(function(e) {
    
        $el = $(this);
        
        // Only proceed with the AJAX nav if the click is the non-current page
        if (!$el.parent().hasClass("active")) {
        
            // Scroll the page up (mostly so they can see the spinner graphic begin)
            $(window).scrollTop(0);
                        
            url = $el.attr("href") + " #inside-content";
            
            $fadeWrapper.animate({ opacity: 0.1 });
            $ajaxLoader.fadeIn(400, function() {
            
                $fadeWrapper.load(url, function() {
                    
                    if ( $el.attr("href") == "index.html" ) {
                        setButton();
                    }
                    else if ( $el.attr("href") == "demo.html" ) {
                        check_ext();
                    }
                    window.location.hash = $el.attr("href");
                    
                    $fadeWrapper.animate({ opacity: 1 });
                    $ajaxLoader.fadeOut();
                
                });
            
                $allListItems.removeClass("active");
                $el.parent().addClass("active");
            
            });
                    
        }
        
        // Make sure the links don't reload the page
        e.preventDefault();
    
    });
    
    function setButton()
    {
        var platf = getPlatform();

        var forstr = "For "+platf.browserName+" on "+platf.osName;
        var additional = "";
        if (platf.browserName == "Firefox")
        {
            if (platf.browserVersion >= 3.5)
            {
                if (platf.osName == "Windows")
                {
                     $('#button_title').text("Add GVod to Firefox");
                     $('strong.forstr').attr("id", "ff_forstr");
                     $('strong.forstr').text(forstr);
                     $('a.dllink').attr("id", "ff_dl"); 
                     $('div.install_button').attr("id", "ff_button"); 
                }
                else if (platf.osName == "Ubuntu Linux")
                {
                     // .deb version
                     $('#button_title').text("Add GVod to Firefox");
                     $('strong.forstr').attr("id", "ff_forstr");
                     $('strong.forstr').text(forstr);
                     $('a.dllink').attr("id", "ff_dl"); 
                     $('div.install_button').attr("id", "ff_button"); 
                }
                else if (platf.osName == "Linux")
                {
                     $('#button_title').text("Add Gvod to Firefox");
                     $('strong.forstr').attr("id", "ff_forstr");
                     $('strong.forstr').text(forstr);
                     $('a.dllink').attr("id", "ff_dl"); 
                     $('div.install_button').attr("id", "ff_button"); 

                     additional = "Please also install the following packages: python-m2crypto python-openssl python-wxgtk2.8 python-apsw python-simplejson";

                }
                else if (platf.osName == "MacOS" && platf.archName == "Intel")
                {
                     $('#button_title').text("Add GVod to Firefox");
                     $('strong.forstr').attr("id", "ff_forstr");
                     $('strong.forstr').text(forstr);
                     $('a.dllink').attr("id", "ff_dl"); 
                     $('div.install_button').attr("id", "ff_button"); 
                     // increase the button size for correct visualization
                     $('#ff_button').css({'width':'300px'});
                     $('a.dllink').css({'width':'263px'});
                }
                else if (platf.osName == "MacOS" && platf.archName == "PowerPC")
                {
                	showErr("Is unfortunately not available for PowerPC");
                }
                else
                {
                	showErr("Is unfortunately not available for your OS");
                }

                $('#ff_dl').click( function() {
                    if (additional != "")
                    {
                        var r = confirm(additional);
                        if (r==true) { go2install(); }
                    }
                    else
                    {
                        go2install();
                    }
                    
                    function go2install()
                    {
                        $(window).scrollTop(0);
                                    
                        url = "install.html" + " #inside-content";
                        
                        $fadeWrapper.animate({ opacity: 0.1 });
                        $ajaxLoader.fadeIn(400, function() {
                        
                            $fadeWrapper.load(url, function() {
                                $allListItems.removeClass("active");
                                generate_page();        
                                
                                window.location.hash = "install.html";
                                
                                $fadeWrapper.animate({ opacity: 1 });
                                $ajaxLoader.fadeOut();
                            
                            });
                        });
                    };
                });
            }
            else
            {
            	additional = "Is unfortunately not available for your version of Firefox";
            }
        }
        
        else if (platf.browserName == "Internet Explorer")
        {
            if (platf.browserVersion >= 7.0)
            {
                if (platf.osName == "Windows")
                {
                	showErr("Will soon be available for IE.");
                }
                else
                {
                    showErr("Is unfortunately not available for your OS.");
                }
            }
            else
            {
            	showErr("Is unfortunately not available for your version of IE.");
            }
        }
        // Other browsers... no dl!
        else
        {
            additional = "It is unfortunately not available for your browser";
            showErr(additional);
            $('#standard_dl').click( function() {
                    alert("The version for your Browser is still not available!");
            });
        }
    
        function showErr(mess)
        {
            // TODO align the message
            if (additional.length > 34)
            {
                $('p.button').css({"margin-bottom":"15px"});
            }
            $('p.button').css({opacity:0.5});
            $('div.install_button').attr("id", "standard_button"); 
            $('strong.forstr').attr("id", "standard_forstr");
            $('strong.forstr').text(" " + mess);
            $('#standard_forstr').prepend("<img src='images/missing_small.png' />");
        };
        
    };
    
    function check_ext()
    {
        if(typeof gvodTransport != 'undefined') 
        {
            if ( !($("#success").children().size() > 0))
            {
                $("#success").append('<img src="images/sp-trans-158.png"><h1 style="padding-left:10px;">Congratulations!</h1><h2> You succesfully installed GVod! </h2>'); 
            }
        }  
        else if ( !($("#success").children().size() > 0))
        {
            $("#success").append('<img src="images/sp-trans-158.png"><h2> GVod is not installed on your machine! Please go on Overview and install it before trying the demo.</h2>');
        }

    };



});




