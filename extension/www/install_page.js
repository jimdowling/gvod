/*
    Written by Riccardo Petrocco 
    based on "Documentation Template" 
    example from css-tricks.com 
*/
	    
function generate_page() 
{
 
    var platf = getPlatform();
    $mainContent = $("#inside-content");
    $mainContent.attr("line-height", "490%");

    //var demopageurl = "http://commons.wikimedia.org/wiki/Commons:Timed_Text_Demo_Page?withJS=MediaWiki:MwEmbed.js"; 
    var demopageurl = "http://en.wikipedia.org/wiki/Polar_bear?withJS=MediaWiki:MwEmbed.js#Population_and_distribution";
    


    var inst = '<h3>Installing SwarmPlayer ';
    inst += ' for '+platf.browserName+' on '+platf.osName;
    inst += '</h3>'; 

    var downloadurl = null;

    if (platf.browserName == "Firefox")
    {
        if (platf.browserVersion >= 3.5)
        {
	        if (platf.osName == "MacOS")
	        {
		         downloadurl = "download/SwarmPlayer_2.0.0w14-mac.xpi";
	         
                 inst += '<ol>';
                 inst += '<li> Click the "Allow" button in the yellow information bar. If this ';
        		 inst += 'bar does not appear, open the downloaded .xpi file with Firefox.';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/fx-mac-infobar.png" width="600">';

        		 inst += '<ol start=2>';
        		 inst += '<li> Click "Install Now" when the Install button becomes active after the countdown.';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/fx-mac-install-popup.png">';

        		 inst += '<ol start=3>';
        		 inst += '<li> Click "Restart Firefox" after the extension has been installed.';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/fx-mac-restart.png">';

        		 inst += '<ol start=4>';
        		 inst += '<li> Close the Add-ons windows, if it opens after the restart.';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/fx-mac-installed.png">';

            	 inst += '<ol start=5>';
            	 inst += '<li> Visit <a href="'+demopageurl+'">a Wikipedia page with P2P video!</a>';
            	 inst += '</ol>';

	        } 
	        else if (platf.osName == "Windows" || platf.osName == "Linux")
            {

                 if (platf.osName == "Linux")
                 {
                     // Not Ubuntu, so advocate .xpi version and tell them to
                     // install the required additional software.
                	 downloadurl = "download/SwarmPlayer_2.0.0w14-linux.xpi";
                 }
                 else if (platf.osName == "Windows")
                 {
                     downloadurl = "download/SwarmPlayer_2.0.0w14-win32.xpi";
                 }

                 inst += '<ol>';
                 inst += '<li> Click the "Allow" button in the yellow information bar. If this ';
        		 inst += 'bar does not appear, open the downloaded .xpi file with Firefox.';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/fx-infobar.png" width="600">';

        		 inst += '<ol start=2>';
        		 inst += '<li> Click "Install Now" when the Install button becomes active after the countdown.';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/fx-install-popup.png">';

        		 inst += '<ol start=3>';
        		 inst += '<li> Click "Restart Firefox" after the extension has been installed.';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/fx-restart.png">';

        		 inst += '<ol start=4>';
        		 inst += '<li> After the restart, close the Add-ons window';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/fx-installed.png">';

                 if (platf.osName == "Linux")
                 {
            		 inst += '<ol start=5>';
            		 inst += '<li> Use your package manager to install the software required to run SwarmPlayer:';
            		 inst += '<ul>';
            		 inst += '   <li> M2Crypto &gt;= 0.16';
            		 inst += '   <li> OpenSSL &gt;= 0.9.8 (with Elliptic Curve Support enabled)';
            		 inst += '   <li> wxPython &gt;= 2.8';
            		 inst += '   <li> apsw &gt;= 3.6';
            		 inst += '</ul>';
            		 inst += 'Easiest way is to use the package manager of your Linux distribution. E.g.';
            		 inst += '<br> <pre> sudo apt-get install python-m2crypto python-openssl python-wxgtk2.8 python-apsw</pre>';
            		 inst += '</ol>';

            		 inst += '<ol start=6>';
            		 inst += '<li> Visit <a href="'+demopageurl+'">a Wikipedia page with P2P video!</a>';
            		 inst += '</ol>';
                 }
		         else
		         {
            		 inst += '<ol start=5>';
            		 inst += '<li> Visit <a href="'+demopageurl+'">a Wikipedia page with P2P video!</a>';
            		 inst += '</ol>';
            	 }
            }
            else if (platf.osName == "Ubuntu Linux")
            {
                 // .deb version
                 downloadurl = "download/SwarmPlayer_2.0.0-1ubuntu14_all.deb";
                 
                 
                 inst += '<ol>';
                 inst += '<li> Click "OK" to open the SwarmPlayer deb file with the GDebi Package Installer.';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/ubuntu-opening.png">';

        		 inst += '<ol start=2>';
        		 inst += '<li> Click "Install Package" when the package has been loaded';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/ubuntu-gdebi-install.png">';

        		 inst += '<ol start=3>';
        		 inst += '<li> Type your password and press Enter.';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/ubuntu-uac.png">';

        		 inst += '<ol start=4>';
        		 inst += '<li> Click "Close" when the installation has finished.';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/ubuntu-gdebi-finished.png">';

        		 inst += '<ol start=5>';
        		 inst += '<li> Close the Package Installer, the Downloads window, and Firefox.';
        		 inst += '<li> Start Firefox again.';
        		 inst += '<li> After Firefox has started, close the Add-ons window';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/ubuntu-installed.png">';

        		 inst += '<ol start=8>';
        		 inst += '<li> Visit <a href="'+demopageurl+'">a Wikipedia page with P2P video!</a>';
        		 inst += '</ol>';
            }
        }	    
    }
    else if (platf.browserName == "Internet Explorer")
    {
        if (platf.browserVersion >= 7.0)
        {
            if (platf.osName == "Windows")
            {
            	// Arno, 2010-07-06: Currently disabled, ../index.html should not refer here.
		        //downloadurl = "SwarmPlayer_IE_2.0.0.exe";

                 inst += '<ol>';
                 inst += '<li> Click on the yellow information bar and select "Download file...".';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/ie-infobar.png">';

        		 inst += '<ol start=2>';
        		 inst += '<li> Click "Run" to download the installer.';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/ie-run-save.png">';

        		 inst += '<ol start=3>';
        		 inst += '<li> Click "Run" after the download has finished.';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/ie-run-dontrun.png">';

        		 inst += '<ol start=4>';
        		 inst += '<li> Click "Close" After the installation is finished';
        		 inst += '</ol>';
        		 inst += '<img class="install" src="images/ie-nsis-close.png">';

        		 inst += '<ol start=5>';
        		 inst += '<li> Visit <a href="'+demopageurl+'">a Wikipedia page with P2P video!</a>';
        		 inst += '</ol>';

             }
         }
    }

    function startdownload()
    {
        window.location = downloadurl;
    }


    // Display install instructions

    inst += '<p style="padding-left: 20px; padding-bottom:20px; font-size:16px; "><em>If the download does not start automatically, please <a href="/download/">click here.</a></em>' ;

    // add the generated content to the page
    $mainContent.append(inst);
    
    // Now start DL
    if (downloadurl != null)
    {
        // If we start supporting Safari, give it time to render page
        window.setTimeout(startdownload, 1000);
        //window.onload = downloadurl;
    }
    else
    {
        document.write("<p><strong>Unfortunately, SwarmPlayer is not (yet) available for your platform.</strong></p>");
    }
    


}

