# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.define "jessie64" do |guest|
    guest.vm.box = "minimal/jessie64"
  end

  config.vm.define "centos7" do |guest|
    guest.vm.box = "minimal/centos7"
  end

  config.vm.define "centos6" do |guest|
    guest.vm.box = "minimal/centos6"
  end

  # https://forums.freebsd.org/threads/52717/
  config.vm.define "freebsd102" do |guest|
    #guest.vm.box = "freebsd/FreeBSD-10.2-RELEASE"
    guest.vm.box = "bento/freebsd-10.2"
    #guest.vm.base_mac = "080027D14C66"
    #guest.vm.synced_folder ".", "/vagrant", id: "vagrant-root", disabled: true
    
    # https://github.com/wunki/vagrant-freebsd/blob/master/Vagrantfile
    #guest.vm.network "private_network", ip: "10.0.1.10"
    #guest.vm.synced_folder ".", "/vagrant", :nfs => true, id: "vagrant-root"
    #guest.vm.box_url = "https://wunki.org/files/freebsd-10.2-amd64-wunki.box"
    #guest.vm.box = "freebsd-10.2-amd64-wunki"
  end

  config.vm.provider :virtualbox do |vb|
    #vb.customize ["modifyvm", :id, "--memory", "1024"]
    #vb.customize ["modifyvm", :id, "--cpus", "1"]
    vb.customize ["modifyvm", :id, "--hwvirtex", "on"]
    vb.customize ["modifyvm", :id, "--audio", "none"]
    vb.customize ["modifyvm", :id, "--nictype1", "virtio"]
    vb.customize ["modifyvm", :id, "--nictype2", "virtio"]
  end

end
