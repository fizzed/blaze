# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  
  config.vm.define "ubuntu14" do |guest|
    guest.vm.box = "minimal/trusty64"
  end

  config.vm.define "ubuntu16", autostart: false do |guest|
    guest.vm.box = "bento/ubuntu-16.04"
  end
  
  config.vm.define "debian8", autostart: false do |guest|
    guest.vm.box = "minimal/jessie64"
  end

  config.vm.define "centos7", autostart: false do |guest|
    guest.vm.box = "minimal/centos7"
    # ssh proxying requires netcat
    guest.vm.provision :shell, inline: "yum install -y nc"
  end

  config.vm.define "centos6", autostart: false do |guest|
    guest.vm.box = "minimal/centos6"
    # ssh proxying requires netcat
    guest.vm.provision :shell, inline: "yum install -y nc"
  end

  config.vm.define "freebsd102", autostart: false do |guest|
    guest.vm.box = "bento/freebsd-10.2"
  end

  config.vm.define "openbsd58", autostart: false do |guest|
    guest.vm.box = "twingly/openbsd-5.8-amd64"
    # Prevent Vagrant from mounting the default /vagrant synced folder
    config.vm.synced_folder '.', '/vagrant', disabled: true
  end

  #config.vm.define "osx1010", autostart: false do |guest|
  #  guest.vm.box = "jhcook/osx-yosemite-10.10"
  #end

end
