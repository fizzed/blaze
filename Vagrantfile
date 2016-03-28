# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.define "debian8" do |guest|
    guest.vm.box = "minimal/jessie64"
  end

  config.vm.define "ubuntu1404" do |guest|
    guest.vm.box = "minimal/trusty64"
  end

  config.vm.define "centos7" do |guest|
    guest.vm.box = "minimal/centos7"
  end

  config.vm.define "centos6" do |guest|
    guest.vm.box = "minimal/centos6"
  end

  config.vm.define "freebsd102" do |guest|
    guest.vm.box = "bento/freebsd-10.2"
  end

end
