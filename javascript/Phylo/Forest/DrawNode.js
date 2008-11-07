Phylo.Forest.DrawNode = function (args) {
	if (args==null) args = {};
	if ( args["node"] != null ) {
		this.Node(args["node"]);
	}
	else {
		this.Node(args);
	}
	return this;
	
}
Phylo.Util.CONSTANT.copyPrototype(Phylo.Forest.DrawNode,Phylo.Forest.Node);

Phylo.Forest.DrawNode.prototype.set_x = function (x) {
    this.x = x;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_y = function (y) {
    this.y = y;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_radius = function (radius) {
    this.radius = radius;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_node_colour = function (node_colour) {
    this.node_colour = node_colour;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_node_shape = function (node_shape) {
    this.node_shape = node_shape;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_node_image = function (node_image) {
    this.node_image = node_image;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_branch_color = function (branch_color) {
    this.branch_color = branch_color;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_branch_shape = function (branch_shape) {
    this.branch_shape = branch_shape;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_branch_width = function (branch_width) {
    this.branch_width = branch_width;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_branch_style = function (branch_style) {
    this.branch_style = branch_style;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_font_face = function (font_face) {
    this.font_face = font_face;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_font_size = function (font_size) {
    this.font_size = font_size;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_font_style = function (font_style) {
    this.font_style = font_style;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_url = function (url) {
    this.url = url;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_text_horiz_offset = function (text_horiz_offset) {
    this.text_horiz_offset = text_horiz_offset;
    return this;
};

Phylo.Forest.DrawNode.prototype.set_text_vert_offset = function (text_vert_offset) {
    this.text_vert_offset = text_vert_offset;
    return this;
};

Phylo.Forest.DrawNode.prototype.get_x = function () { return this.x };

Phylo.Forest.DrawNode.prototype.get_y = function () { return this.y };

Phylo.Forest.DrawNode.prototype.get_radius = function () { return this.radius };

Phylo.Forest.DrawNode.prototype.get_node_colour = function () { return this.node_colour };

Phylo.Forest.DrawNode.prototype.get_node_shape = function () { return this.node_shape };

Phylo.Forest.DrawNode.prototype.get_node_image = function () { return this.node_image };

Phylo.Forest.DrawNode.prototype.get_branch_color = function () { return this.branch_color };

Phylo.Forest.DrawNode.prototype.get_branch_shape = function () { return this.branch_shape };

Phylo.Forest.DrawNode.prototype.get_branch_width = function () { return this.branch_width };

Phylo.Forest.DrawNode.prototype.get_branch_style = function () { return this.branch_style };

Phylo.Forest.DrawNode.prototype.get_font_face = function () { return this.font_face };

Phylo.Forest.DrawNode.prototype.get_font_size = function () { return this.font_size };

Phylo.Forest.DrawNode.prototype.get_font_style = function () { return this.font_style };

Phylo.Forest.DrawNode.prototype.get_url = function () { return this.url };

Phylo.Forest.DrawNode.prototype.get_text_horiz_offset = function () { return this.text_horiz_offset };

Phylo.Forest.DrawNode.prototype.get_text_vert_offset = function () { return this.text_vert_offset };