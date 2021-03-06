//
// Autogenerated by Thrift Compiler (0.8.0)
//
// DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
//
var Thrift = require('thrift').Thrift;

var ttypes = require('./flowservices_types');
//HELPER FUNCTIONS AND STRUCTURES

var FARService_init_args = function(args) {
  this.token = null;
  this.info = null;
  if (args) {
    if (args.token !== undefined) {
      this.token = args.token;
    }
    if (args.info !== undefined) {
      this.info = args.info;
    }
  }
};
FARService_init_args.prototype = {};
FARService_init_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.token = new ttypes.DelegationToken();
        this.token.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRUCT) {
        this.info = new ttypes.ResourceInfo();
        this.info.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_init_args.prototype.write = function(output) {
  output.writeStructBegin('FARService_init_args');
  if (this.token) {
    output.writeFieldBegin('token', Thrift.Type.STRUCT, 1);
    this.token.write(output);
    output.writeFieldEnd();
  }
  if (this.info) {
    output.writeFieldBegin('info', Thrift.Type.STRUCT, 2);
    this.info.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_init_result = function(args) {
  this.success = null;
  this.e = null;
  if (args) {
    if (args.success !== undefined) {
      this.success = args.success;
    }
    if (args.e !== undefined) {
      this.e = args.e;
    }
  }
};
FARService_init_result.prototype = {};
FARService_init_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 0:
      if (ftype == Thrift.Type.STRUCT) {
        this.success = new ttypes.ResourceIdentifier();
        this.success.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.e = new ttypes.FARServiceException();
        this.e.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_init_result.prototype.write = function(output) {
  output.writeStructBegin('FARService_init_result');
  if (this.success) {
    output.writeFieldBegin('success', Thrift.Type.STRUCT, 0);
    this.success.write(output);
    output.writeFieldEnd();
  }
  if (this.e) {
    output.writeFieldBegin('e', Thrift.Type.STRUCT, 1);
    this.e.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_chunk_args = function(args) {
  this.token = null;
  this.resource = null;
  this.chunk = null;
  if (args) {
    if (args.token !== undefined) {
      this.token = args.token;
    }
    if (args.resource !== undefined) {
      this.resource = args.resource;
    }
    if (args.chunk !== undefined) {
      this.chunk = args.chunk;
    }
  }
};
FARService_chunk_args.prototype = {};
FARService_chunk_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.token = new ttypes.DelegationToken();
        this.token.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRUCT) {
        this.resource = new ttypes.ResourceIdentifier();
        this.resource.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 3:
      if (ftype == Thrift.Type.STRING) {
        this.chunk = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_chunk_args.prototype.write = function(output) {
  output.writeStructBegin('FARService_chunk_args');
  if (this.token) {
    output.writeFieldBegin('token', Thrift.Type.STRUCT, 1);
    this.token.write(output);
    output.writeFieldEnd();
  }
  if (this.resource) {
    output.writeFieldBegin('resource', Thrift.Type.STRUCT, 2);
    this.resource.write(output);
    output.writeFieldEnd();
  }
  if (this.chunk) {
    output.writeFieldBegin('chunk', Thrift.Type.STRING, 3);
    output.writeString(this.chunk);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_chunk_result = function(args) {
  this.e = null;
  if (args) {
    if (args.e !== undefined) {
      this.e = args.e;
    }
  }
};
FARService_chunk_result.prototype = {};
FARService_chunk_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.e = new ttypes.FARServiceException();
        this.e.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_chunk_result.prototype.write = function(output) {
  output.writeStructBegin('FARService_chunk_result');
  if (this.e) {
    output.writeFieldBegin('e', Thrift.Type.STRUCT, 1);
    this.e.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_deploy_args = function(args) {
  this.token = null;
  this.resource = null;
  if (args) {
    if (args.token !== undefined) {
      this.token = args.token;
    }
    if (args.resource !== undefined) {
      this.resource = args.resource;
    }
  }
};
FARService_deploy_args.prototype = {};
FARService_deploy_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.token = new ttypes.DelegationToken();
        this.token.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRUCT) {
        this.resource = new ttypes.ResourceIdentifier();
        this.resource.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_deploy_args.prototype.write = function(output) {
  output.writeStructBegin('FARService_deploy_args');
  if (this.token) {
    output.writeFieldBegin('token', Thrift.Type.STRUCT, 1);
    this.token.write(output);
    output.writeFieldEnd();
  }
  if (this.resource) {
    output.writeFieldBegin('resource', Thrift.Type.STRUCT, 2);
    this.resource.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_deploy_result = function(args) {
  this.e = null;
  if (args) {
    if (args.e !== undefined) {
      this.e = args.e;
    }
  }
};
FARService_deploy_result.prototype = {};
FARService_deploy_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.e = new ttypes.FARServiceException();
        this.e.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_deploy_result.prototype.write = function(output) {
  output.writeStructBegin('FARService_deploy_result');
  if (this.e) {
    output.writeFieldBegin('e', Thrift.Type.STRUCT, 1);
    this.e.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_status_args = function(args) {
  this.token = null;
  this.resource = null;
  if (args) {
    if (args.token !== undefined) {
      this.token = args.token;
    }
    if (args.resource !== undefined) {
      this.resource = args.resource;
    }
  }
};
FARService_status_args.prototype = {};
FARService_status_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.token = new ttypes.DelegationToken();
        this.token.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRUCT) {
        this.resource = new ttypes.ResourceIdentifier();
        this.resource.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_status_args.prototype.write = function(output) {
  output.writeStructBegin('FARService_status_args');
  if (this.token) {
    output.writeFieldBegin('token', Thrift.Type.STRUCT, 1);
    this.token.write(output);
    output.writeFieldEnd();
  }
  if (this.resource) {
    output.writeFieldBegin('resource', Thrift.Type.STRUCT, 2);
    this.resource.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_status_result = function(args) {
  this.success = null;
  this.e = null;
  if (args) {
    if (args.success !== undefined) {
      this.success = args.success;
    }
    if (args.e !== undefined) {
      this.e = args.e;
    }
  }
};
FARService_status_result.prototype = {};
FARService_status_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 0:
      if (ftype == Thrift.Type.STRUCT) {
        this.success = new ttypes.FARStatus();
        this.success.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.e = new ttypes.FARServiceException();
        this.e.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_status_result.prototype.write = function(output) {
  output.writeStructBegin('FARService_status_result');
  if (this.success) {
    output.writeFieldBegin('success', Thrift.Type.STRUCT, 0);
    this.success.write(output);
    output.writeFieldEnd();
  }
  if (this.e) {
    output.writeFieldBegin('e', Thrift.Type.STRUCT, 1);
    this.e.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_promote_args = function(args) {
  this.token = null;
  this.identifier = null;
  if (args) {
    if (args.token !== undefined) {
      this.token = args.token;
    }
    if (args.identifier !== undefined) {
      this.identifier = args.identifier;
    }
  }
};
FARService_promote_args.prototype = {};
FARService_promote_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.token = new ttypes.DelegationToken();
        this.token.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRUCT) {
        this.identifier = new ttypes.FlowIdentifier();
        this.identifier.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_promote_args.prototype.write = function(output) {
  output.writeStructBegin('FARService_promote_args');
  if (this.token) {
    output.writeFieldBegin('token', Thrift.Type.STRUCT, 1);
    this.token.write(output);
    output.writeFieldEnd();
  }
  if (this.identifier) {
    output.writeFieldBegin('identifier', Thrift.Type.STRUCT, 2);
    this.identifier.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_promote_result = function(args) {
  this.success = null;
  this.e = null;
  if (args) {
    if (args.success !== undefined) {
      this.success = args.success;
    }
    if (args.e !== undefined) {
      this.e = args.e;
    }
  }
};
FARService_promote_result.prototype = {};
FARService_promote_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 0:
      if (ftype == Thrift.Type.BOOL) {
        this.success = input.readBool();
      } else {
        input.skip(ftype);
      }
      break;
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.e = new ttypes.FARServiceException();
        this.e.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_promote_result.prototype.write = function(output) {
  output.writeStructBegin('FARService_promote_result');
  if (this.success) {
    output.writeFieldBegin('success', Thrift.Type.BOOL, 0);
    output.writeBool(this.success);
    output.writeFieldEnd();
  }
  if (this.e) {
    output.writeFieldBegin('e', Thrift.Type.STRUCT, 1);
    this.e.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_remove_args = function(args) {
  this.token = null;
  this.identifier = null;
  if (args) {
    if (args.token !== undefined) {
      this.token = args.token;
    }
    if (args.identifier !== undefined) {
      this.identifier = args.identifier;
    }
  }
};
FARService_remove_args.prototype = {};
FARService_remove_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.token = new ttypes.DelegationToken();
        this.token.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRUCT) {
        this.identifier = new ttypes.FlowIdentifier();
        this.identifier.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_remove_args.prototype.write = function(output) {
  output.writeStructBegin('FARService_remove_args');
  if (this.token) {
    output.writeFieldBegin('token', Thrift.Type.STRUCT, 1);
    this.token.write(output);
    output.writeFieldEnd();
  }
  if (this.identifier) {
    output.writeFieldBegin('identifier', Thrift.Type.STRUCT, 2);
    this.identifier.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_remove_result = function(args) {
  this.e = null;
  if (args) {
    if (args.e !== undefined) {
      this.e = args.e;
    }
  }
};
FARService_remove_result.prototype = {};
FARService_remove_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.e = new ttypes.FARServiceException();
        this.e.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_remove_result.prototype.write = function(output) {
  output.writeStructBegin('FARService_remove_result');
  if (this.e) {
    output.writeFieldBegin('e', Thrift.Type.STRUCT, 1);
    this.e.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_removeAll_args = function(args) {
  this.token = null;
  this.accountId = null;
  if (args) {
    if (args.token !== undefined) {
      this.token = args.token;
    }
    if (args.accountId !== undefined) {
      this.accountId = args.accountId;
    }
  }
};
FARService_removeAll_args.prototype = {};
FARService_removeAll_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.token = new ttypes.DelegationToken();
        this.token.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.accountId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_removeAll_args.prototype.write = function(output) {
  output.writeStructBegin('FARService_removeAll_args');
  if (this.token) {
    output.writeFieldBegin('token', Thrift.Type.STRUCT, 1);
    this.token.write(output);
    output.writeFieldEnd();
  }
  if (this.accountId) {
    output.writeFieldBegin('accountId', Thrift.Type.STRING, 2);
    output.writeString(this.accountId);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_removeAll_result = function(args) {
  this.e = null;
  if (args) {
    if (args.e !== undefined) {
      this.e = args.e;
    }
  }
};
FARService_removeAll_result.prototype = {};
FARService_removeAll_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.e = new ttypes.FARServiceException();
        this.e.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_removeAll_result.prototype.write = function(output) {
  output.writeStructBegin('FARService_removeAll_result');
  if (this.e) {
    output.writeFieldBegin('e', Thrift.Type.STRUCT, 1);
    this.e.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_reset_args = function(args) {
  this.token = null;
  this.accountId = null;
  if (args) {
    if (args.token !== undefined) {
      this.token = args.token;
    }
    if (args.accountId !== undefined) {
      this.accountId = args.accountId;
    }
  }
};
FARService_reset_args.prototype = {};
FARService_reset_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.token = new ttypes.DelegationToken();
        this.token.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.accountId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_reset_args.prototype.write = function(output) {
  output.writeStructBegin('FARService_reset_args');
  if (this.token) {
    output.writeFieldBegin('token', Thrift.Type.STRUCT, 1);
    this.token.write(output);
    output.writeFieldEnd();
  }
  if (this.accountId) {
    output.writeFieldBegin('accountId', Thrift.Type.STRING, 2);
    output.writeString(this.accountId);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARService_reset_result = function(args) {
  this.e = null;
  if (args) {
    if (args.e !== undefined) {
      this.e = args.e;
    }
  }
};
FARService_reset_result.prototype = {};
FARService_reset_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.e = new ttypes.FARServiceException();
        this.e.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FARService_reset_result.prototype.write = function(output) {
  output.writeStructBegin('FARService_reset_result');
  if (this.e) {
    output.writeFieldBegin('e', Thrift.Type.STRUCT, 1);
    this.e.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var FARServiceClient = exports.Client = function(output, pClass) {
    this.output = output;
    this.pClass = pClass;
    this.seqid = 0;
    this._reqs = {};
};
FARServiceClient.prototype = {};
FARServiceClient.prototype.init = function(token, info, callback) {
  this.seqid += 1;
  this._reqs[this.seqid] = callback;
  this.send_init(token, info);
};

FARServiceClient.prototype.send_init = function(token, info) {
  var output = new this.pClass(this.output);
  output.writeMessageBegin('init', Thrift.MessageType.CALL, this.seqid);
  var args = new FARService_init_args();
  args.token = token;
  args.info = info;
  args.write(output);
  output.writeMessageEnd();
  return this.output.flush();
};

FARServiceClient.prototype.recv_init = function(input,mtype,rseqid) {
  var callback = this._reqs[rseqid] || function() {};
  delete this._reqs[rseqid];
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(input);
    input.readMessageEnd();
    return callback(x);
  }
  var result = new FARService_init_result();
  result.read(input);
  input.readMessageEnd();

  if (null !== result.e) {
    return callback(result.e);
  }
  if (null !== result.success) {
    return callback(null, result.success);
  }
  return callback('init failed: unknown result');
};
FARServiceClient.prototype.chunk = function(token, resource, chunk, callback) {
  this.seqid += 1;
  this._reqs[this.seqid] = callback;
  this.send_chunk(token, resource, chunk);
};

FARServiceClient.prototype.send_chunk = function(token, resource, chunk) {
  var output = new this.pClass(this.output);
  output.writeMessageBegin('chunk', Thrift.MessageType.CALL, this.seqid);
  var args = new FARService_chunk_args();
  args.token = token;
  args.resource = resource;
  args.chunk = chunk;
  args.write(output);
  output.writeMessageEnd();
  return this.output.flush();
};

FARServiceClient.prototype.recv_chunk = function(input,mtype,rseqid) {
  var callback = this._reqs[rseqid] || function() {};
  delete this._reqs[rseqid];
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(input);
    input.readMessageEnd();
    return callback(x);
  }
  var result = new FARService_chunk_result();
  result.read(input);
  input.readMessageEnd();

  if (null !== result.e) {
    return callback(result.e);
  }
  callback(null)
};
FARServiceClient.prototype.deploy = function(token, resource, callback) {
  this.seqid += 1;
  this._reqs[this.seqid] = callback;
  this.send_deploy(token, resource);
};

FARServiceClient.prototype.send_deploy = function(token, resource) {
  var output = new this.pClass(this.output);
  output.writeMessageBegin('deploy', Thrift.MessageType.CALL, this.seqid);
  var args = new FARService_deploy_args();
  args.token = token;
  args.resource = resource;
  args.write(output);
  output.writeMessageEnd();
  return this.output.flush();
};

FARServiceClient.prototype.recv_deploy = function(input,mtype,rseqid) {
  var callback = this._reqs[rseqid] || function() {};
  delete this._reqs[rseqid];
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(input);
    input.readMessageEnd();
    return callback(x);
  }
  var result = new FARService_deploy_result();
  result.read(input);
  input.readMessageEnd();

  if (null !== result.e) {
    return callback(result.e);
  }
  callback(null)
};
FARServiceClient.prototype.status = function(token, resource, callback) {
  this.seqid += 1;
  this._reqs[this.seqid] = callback;
  this.send_status(token, resource);
};

FARServiceClient.prototype.send_status = function(token, resource) {
  var output = new this.pClass(this.output);
  output.writeMessageBegin('status', Thrift.MessageType.CALL, this.seqid);
  var args = new FARService_status_args();
  args.token = token;
  args.resource = resource;
  args.write(output);
  output.writeMessageEnd();
  return this.output.flush();
};

FARServiceClient.prototype.recv_status = function(input,mtype,rseqid) {
  var callback = this._reqs[rseqid] || function() {};
  delete this._reqs[rseqid];
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(input);
    input.readMessageEnd();
    return callback(x);
  }
  var result = new FARService_status_result();
  result.read(input);
  input.readMessageEnd();

  if (null !== result.e) {
    return callback(result.e);
  }
  if (null !== result.success) {
    return callback(null, result.success);
  }
  return callback('status failed: unknown result');
};
FARServiceClient.prototype.promote = function(token, identifier, callback) {
  this.seqid += 1;
  this._reqs[this.seqid] = callback;
  this.send_promote(token, identifier);
};

FARServiceClient.prototype.send_promote = function(token, identifier) {
  var output = new this.pClass(this.output);
  output.writeMessageBegin('promote', Thrift.MessageType.CALL, this.seqid);
  var args = new FARService_promote_args();
  args.token = token;
  args.identifier = identifier;
  args.write(output);
  output.writeMessageEnd();
  return this.output.flush();
};

FARServiceClient.prototype.recv_promote = function(input,mtype,rseqid) {
  var callback = this._reqs[rseqid] || function() {};
  delete this._reqs[rseqid];
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(input);
    input.readMessageEnd();
    return callback(x);
  }
  var result = new FARService_promote_result();
  result.read(input);
  input.readMessageEnd();

  if (null !== result.e) {
    return callback(result.e);
  }
  if (null !== result.success) {
    return callback(null, result.success);
  }
  return callback('promote failed: unknown result');
};
FARServiceClient.prototype.remove = function(token, identifier, callback) {
  this.seqid += 1;
  this._reqs[this.seqid] = callback;
  this.send_remove(token, identifier);
};

FARServiceClient.prototype.send_remove = function(token, identifier) {
  var output = new this.pClass(this.output);
  output.writeMessageBegin('remove', Thrift.MessageType.CALL, this.seqid);
  var args = new FARService_remove_args();
  args.token = token;
  args.identifier = identifier;
  args.write(output);
  output.writeMessageEnd();
  return this.output.flush();
};

FARServiceClient.prototype.recv_remove = function(input,mtype,rseqid) {
  var callback = this._reqs[rseqid] || function() {};
  delete this._reqs[rseqid];
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(input);
    input.readMessageEnd();
    return callback(x);
  }
  var result = new FARService_remove_result();
  result.read(input);
  input.readMessageEnd();

  if (null !== result.e) {
    return callback(result.e);
  }
  callback(null)
};
FARServiceClient.prototype.removeAll = function(token, accountId, callback) {
  this.seqid += 1;
  this._reqs[this.seqid] = callback;
  this.send_removeAll(token, accountId);
};

FARServiceClient.prototype.send_removeAll = function(token, accountId) {
  var output = new this.pClass(this.output);
  output.writeMessageBegin('removeAll', Thrift.MessageType.CALL, this.seqid);
  var args = new FARService_removeAll_args();
  args.token = token;
  args.accountId = accountId;
  args.write(output);
  output.writeMessageEnd();
  return this.output.flush();
};

FARServiceClient.prototype.recv_removeAll = function(input,mtype,rseqid) {
  var callback = this._reqs[rseqid] || function() {};
  delete this._reqs[rseqid];
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(input);
    input.readMessageEnd();
    return callback(x);
  }
  var result = new FARService_removeAll_result();
  result.read(input);
  input.readMessageEnd();

  if (null !== result.e) {
    return callback(result.e);
  }
  callback(null)
};
FARServiceClient.prototype.reset = function(token, accountId, callback) {
  this.seqid += 1;
  this._reqs[this.seqid] = callback;
  this.send_reset(token, accountId);
};

FARServiceClient.prototype.send_reset = function(token, accountId) {
  var output = new this.pClass(this.output);
  output.writeMessageBegin('reset', Thrift.MessageType.CALL, this.seqid);
  var args = new FARService_reset_args();
  args.token = token;
  args.accountId = accountId;
  args.write(output);
  output.writeMessageEnd();
  return this.output.flush();
};

FARServiceClient.prototype.recv_reset = function(input,mtype,rseqid) {
  var callback = this._reqs[rseqid] || function() {};
  delete this._reqs[rseqid];
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(input);
    input.readMessageEnd();
    return callback(x);
  }
  var result = new FARService_reset_result();
  result.read(input);
  input.readMessageEnd();

  if (null !== result.e) {
    return callback(result.e);
  }
  callback(null)
};
var FARServiceProcessor = exports.Processor = function(handler) {
  this._handler = handler
}
FARServiceProcessor.prototype.process = function(input, output) {
  var r = input.readMessageBegin();
  if (this['process_' + r.fname]) {
    return this['process_' + r.fname].call(this, r.rseqid, input, output);
  } else {
    input.skip(Thrift.Type.STRUCT);
    input.readMessageEnd();
    var x = new Thrift.TApplicationException(Thrift.TApplicationExceptionType.UNKNOWN_METHOD, 'Unknown function ' + r.fname);
    output.writeMessageBegin(r.fname, Thrift.MessageType.Exception, r.rseqid);
    x.write(output);
    output.writeMessageEnd();
    output.flush();
  }
}

FARServiceProcessor.prototype.process_init = function(seqid, input, output) {
  var args = new FARService_init_args();
  args.read(input);
  input.readMessageEnd();
  var result = new FARService_init_result();
  this._handler.init(args.token, args.info, function (success) {
    result.success = success;
    output.writeMessageBegin("init", Thrift.MessageType.REPLY, seqid);
    result.write(output);
    output.writeMessageEnd();
    output.flush();
  })
}

FARServiceProcessor.prototype.process_chunk = function(seqid, input, output) {
  var args = new FARService_chunk_args();
  args.read(input);
  input.readMessageEnd();
  var result = new FARService_chunk_result();
  this._handler.chunk(args.token, args.resource, args.chunk, function (success) {
    result.success = success;
    output.writeMessageBegin("chunk", Thrift.MessageType.REPLY, seqid);
    result.write(output);
    output.writeMessageEnd();
    output.flush();
  })
}

FARServiceProcessor.prototype.process_deploy = function(seqid, input, output) {
  var args = new FARService_deploy_args();
  args.read(input);
  input.readMessageEnd();
  var result = new FARService_deploy_result();
  this._handler.deploy(args.token, args.resource, function (success) {
    result.success = success;
    output.writeMessageBegin("deploy", Thrift.MessageType.REPLY, seqid);
    result.write(output);
    output.writeMessageEnd();
    output.flush();
  })
}

FARServiceProcessor.prototype.process_status = function(seqid, input, output) {
  var args = new FARService_status_args();
  args.read(input);
  input.readMessageEnd();
  var result = new FARService_status_result();
  this._handler.status(args.token, args.resource, function (success) {
    result.success = success;
    output.writeMessageBegin("status", Thrift.MessageType.REPLY, seqid);
    result.write(output);
    output.writeMessageEnd();
    output.flush();
  })
}

FARServiceProcessor.prototype.process_promote = function(seqid, input, output) {
  var args = new FARService_promote_args();
  args.read(input);
  input.readMessageEnd();
  var result = new FARService_promote_result();
  this._handler.promote(args.token, args.identifier, function (success) {
    result.success = success;
    output.writeMessageBegin("promote", Thrift.MessageType.REPLY, seqid);
    result.write(output);
    output.writeMessageEnd();
    output.flush();
  })
}

FARServiceProcessor.prototype.process_remove = function(seqid, input, output) {
  var args = new FARService_remove_args();
  args.read(input);
  input.readMessageEnd();
  var result = new FARService_remove_result();
  this._handler.remove(args.token, args.identifier, function (success) {
    result.success = success;
    output.writeMessageBegin("remove", Thrift.MessageType.REPLY, seqid);
    result.write(output);
    output.writeMessageEnd();
    output.flush();
  })
}

FARServiceProcessor.prototype.process_removeAll = function(seqid, input, output) {
  var args = new FARService_removeAll_args();
  args.read(input);
  input.readMessageEnd();
  var result = new FARService_removeAll_result();
  this._handler.removeAll(args.token, args.accountId, function (success) {
    result.success = success;
    output.writeMessageBegin("removeAll", Thrift.MessageType.REPLY, seqid);
    result.write(output);
    output.writeMessageEnd();
    output.flush();
  })
}

FARServiceProcessor.prototype.process_reset = function(seqid, input, output) {
  var args = new FARService_reset_args();
  args.read(input);
  input.readMessageEnd();
  var result = new FARService_reset_result();
  this._handler.reset(args.token, args.accountId, function (success) {
    result.success = success;
    output.writeMessageBegin("reset", Thrift.MessageType.REPLY, seqid);
    result.write(output);
    output.writeMessageEnd();
    output.flush();
  })
}

