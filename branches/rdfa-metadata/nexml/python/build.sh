# this script is not at all interesting outside of the webserver.
# all it does is that it creates a python sdist, moves it into
# the downloads folder and cleans up after itself
python setup.py sdist
python setup.py clean
mv dist/* ../downloads
rm -rf pynexml.egg-info ez_setup.pyc dist pynexml-0.01 setuptools-0.6c3-py2.3.egg
