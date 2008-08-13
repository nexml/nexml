SUBDIRS = cpp

EXTRA_DIST = examples html xsd

dist-hook:
	find "$(distdir)/examples" -depth -and -name .svn -and -type d -and -exec rm -rf {} \; 
	find "$(distdir)/html" -depth -and -name .svn -and -type d -and -exec rm -rf {} \; 
	find "$(distdir)/xsd" -depth -and -name .svn -and -type d -and -exec rm -rf {} \; 

