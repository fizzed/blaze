
 - First class support for vagrant.  Automatic support for 'vagrant+ssh' uri
   syntax for SecureShells.sshConnect().
 - Annotations for tasks (ordering, descriptions, arguments?)
 - Move ivy dependency to its own module (blaze-ivy)
 - Examples on using blaze in an embedded mode vs. scripting mode
 - Caching of dependency lookups.  If input is same + no snapshots in use then
   the last returned result is safe to use.
