# -*- coding: utf-8 -*-

# http://docs.fabfile.org/en/1.5/tutorial.html

from fabric.api import *
from fabric.contrib.files import exists

project = "adrenaline_club"

# the user to use for the remote commands
env.user = 'ryankirkman'
# the servers where the commands are executed
env.hosts = ['104.197.44.2']
env.key_filename = '~/.ssh/google_compute_engine'


def reset():
    """
    Reset local debug env.
    """

    local("rm -rf /tmp/instance")
    local("mkdir /tmp/instance")
    local("python manage.py initdb")


def setup():
    """
    Setup virtual env.
    """

    local("virtualenv env")
    activate_this = "env/bin/activate_this.py"
    execfile(activate_this, dict(__file__=activate_this))
    local("python setup.py install")
    reset()


def remote_setup():
    if not exists(directory, use_sudo=True):
        print directory + ' exists'


def d():
    """
    Debug.
    """

    # reset()
    local("python manage.py run")


def babel():
    """
    Babel compile.
    """

    local("python setup.py compile_catalog --directory `find -name translations` --locale zh -f")


def pack():
    # create a new source distribution as tarball
    local('python setup.py sdist --formats=gztar', capture=False)


def deploy():
    # figure out the release name and version
    dist = local('python setup.py --fullname', capture=True).strip()
    # upload the source tarball to the temporary folder on the server
    put('dist/%s.tar.gz' % dist, '/tmp/fbone.tar.gz')
    # create a place where we can unzip the tarball, then enter
    # that directory and unzip it
    directory = '/tmp/fbone-app'
    if not exists(directory):
        run('mkdir ' + directory)

    with cd(directory):
        run('tar xzf /tmp/fbone.tar.gz')
        # now setup the package with our virtual environment's
        # python interpreter
        sudo('/var/www/fbone/env/bin/python setup.py install')
    # now that all is set up, delete the folder again
    run('rm -rf /tmp/fbone /tmp/fbone.tar.gz')
    # and finally touch the .wsgi file so that mod_wsgi triggers
    # a reload of the application
    run('touch /var/www/fbone/app.wsgi' % project)
