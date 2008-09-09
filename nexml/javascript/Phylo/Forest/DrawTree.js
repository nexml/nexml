Phylo.Forest.DrawTree = function (args) {
	if (args==null) args = {};
	if ( args["tree"] != null ) {
		this.Tree(args["tree"]);
		for ( var key in args ) {
			if ( key != "tree" ) {
				this[key] = args[key];
			}
		}
		var draw_nodes = new Array();
		var new_id_of = {};
		args["tree"].visit_depth_first({
			'pre':function(n){
				var draw_node = new Phylo.Forest.DrawNode({'node':n});
				draw_nodes.push(draw_node);
				new_id_of[n.get_id()] = draw_node.get_id();
				if ( ! n.is_root() ) {
					var parent_id = n.get_parent().get_id();
					for ( var i = draw_nodes.length - 1; i >= 0; i-- ) {
						if ( draw_nodes[i].get_id() == new_id_of[parent_id] ) {
							draw_node.set_parent(draw_nodes[i]);
							draw_nodes[i].set_child(draw_node);
							break;
						}
					}
				}
			}	
		});
		this.insert(draw_nodes);
	}
	else {
		this.Phylo.Forest.Tree(args);
	}
	return this;
	
}
copyPrototype(Phylo.Forest.DrawTree,Phylo.Forest.Tree);

Phylo.Forest.DrawTree.prototype.set_width = function (width) { // XXX
    this.width = width;
    this._redraw();
    return this;
};

Phylo.Forest.DrawTree.prototype.set_height = function (height) { // XXX
    this.height = height;
    this._redraw();
    return this;
};

Phylo.Forest.DrawTree.prototype.set_node_radius = function (node_radius) {
    this.node_radius = node_radius;
    this._apply_to_nodes('node_radius',node_radius);
    return this;
};

Phylo.Forest.DrawTree.prototype.set_node_colour = function (node_colour) {
    this.node_colour = node_colour;
    this._apply_to_nodes('node_colour',node_colour);
    return this;
};

Phylo.Forest.DrawTree.prototype.set_node_shape = function (node_shape) {
    this.node_shape = node_shape;
    this._apply_to_nodes('node_shape',node_shape);
    return this;
};

Phylo.Forest.DrawTree.prototype.set_node_image = function (node_image) {
    this.node_image = node_image;
    this._apply_to_nodes('node_image',node_image);
    return this;
};

Phylo.Forest.DrawTree.prototype.set_branch_color = function (branch_color) {
    this.branch_color = branch_color;
    this._apply_to_nodes('branch_color',branch_color);
    return this;
};

Phylo.Forest.DrawTree.prototype.set_branch_shape = function (branch_shape) {
    this.branch_shape = branch_shape;
    this._apply_to_nodes('branch_shape',branch_shape);
    return this;
};

Phylo.Forest.DrawTree.prototype.set_branch_width = function (branch_width) {
    this.branch_width = branch_width;
    this._apply_to_nodes('branch_width',branch_width);
    return this;
};

Phylo.Forest.DrawTree.prototype.set_branch_style = function (branch_style) {
    this.branch_style = branch_style;
    this._apply_to_nodes('branch_style',branch_style);
    return this;
};

Phylo.Forest.DrawTree.prototype.set_font_face = function (font_face) {
    this.font_face = font_face;
    this._apply_to_nodes('font_face',font_face);
    return this;
};

Phylo.Forest.DrawTree.prototype.set_font_size = function (font_size) {
    this.font_size = font_size;
    this._apply_to_nodes('font_size',font_size);
    return this;
};

Phylo.Forest.DrawTree.prototype.set_font_style = function (font_style) {
    this.font_style = font_style;
    this._apply_to_nodes('font_style',font_style);
    return this;
};

Phylo.Forest.DrawTree.prototype.set_margin = function (margin) { // XXX
    this.margin = margin;
    this._redraw();
    return this;
};

Phylo.Forest.DrawTree.prototype.set_margin_top = function (margin_top) { // XXX
    this.margin_top = margin_top;
    this._redraw();
    return this;
};

Phylo.Forest.DrawTree.prototype.set_margin_bottom = function (margin_bottom) { //XXX
    this.margin_bottom = margin_bottom;
    this._redraw();
    return this;
};

Phylo.Forest.DrawTree.prototype.set_margin_left = function (margin_left) { //XXX
    this.margin_left = margin_left;
    this._redraw();
    return this;
};

Phylo.Forest.DrawTree.prototype.set_margin_right = function (margin_right) { //XXX
    this.margin_right = margin_right;
    this._redraw();
    return this;
};

Phylo.Forest.DrawTree.prototype.set_padding = function (padding) { //XXX
    this.padding = padding;
    this._redraw();
    return this;
};

Phylo.Forest.DrawTree.prototype.set_padding_top = function (padding_top) { //XXX
    this.padding_top = padding_top;
    this._redraw();
    return this;
};

Phylo.Forest.DrawTree.prototype.set_padding_bottom = function (padding_bottom) { //XXX
    this.padding_bottom = padding_bottom;
    this._redraw();
    return this;
};

Phylo.Forest.DrawTree.prototype.set_padding_left = function (padding_left) { //XXX
    this.padding_left = padding_left;
    this._redraw();
    return this;
};

Phylo.Forest.DrawTree.prototype.set_padding_right = function (padding_right) { //XXX
    this.padding_right = padding_right;
    this._redraw();
    return this;
};

Phylo.Forest.DrawTree.prototype.set_mode = function (mode) { //XXX
    this.mode = mode;
    this._redraw();
    return this;
};

Phylo.Forest.DrawTree.prototype.set_shape = function (shape) {
    this.shape = shape;
    return this;
};

Phylo.Forest.DrawTree.prototype.set_text_horiz_offset = function (text_horiz_offset) { //XXX
    this.text_horiz_offset = text_horiz_offset;
    this._apply_to_nodes('text_horiz_offset',text_horiz_offset);
    return this;
};

Phylo.Forest.DrawTree.prototype.set_text_vert_offset = function (text_vert_offset) { //XXX
    this.text_vert_offset = text_vert_offset;
    this._apply_to_nodes('text_vert_offset',text_vert_offset); 
    return this;
};

Phylo.Forest.DrawTree.prototype.get_width = function () { return this.width };

Phylo.Forest.DrawTree.prototype.get_height = function () { return this.height };

Phylo.Forest.DrawTree.prototype.get_node_radius = function () { return this.node_radius };

Phylo.Forest.DrawTree.prototype.get_node_colour = function () { return this.node_colour };

Phylo.Forest.DrawTree.prototype.get_node_shape = function () { return this.node_shape };

Phylo.Forest.DrawTree.prototype.get_node_image = function () { return this.node_image };

Phylo.Forest.DrawTree.prototype.get_branch_color = function () { return this.branch_color };

Phylo.Forest.DrawTree.prototype.get_branch_shape = function () { return this.branch_shape };

Phylo.Forest.DrawTree.prototype.get_branch_width = function () { return this.branch_width };

Phylo.Forest.DrawTree.prototype.get_branch_style = function () { return this.branch_style };

Phylo.Forest.DrawTree.prototype.get_font_face = function () { return this.font_face };

Phylo.Forest.DrawTree.prototype.get_font_size = function () { return this.font_size };

Phylo.Forest.DrawTree.prototype.get_font_style = function () { return this.font_style };

Phylo.Forest.DrawTree.prototype.get_margin = function () { return this.margin };

Phylo.Forest.DrawTree.prototype.get_margin_top = function () { return this.margin_top };

Phylo.Forest.DrawTree.prototype.get_margin_bottom = function () { return this.margin_bottom };

Phylo.Forest.DrawTree.prototype.get_margin_left = function () { return this.margin_left };

Phylo.Forest.DrawTree.prototype.get_margin_right = function () { return this.margin_right };

Phylo.Forest.DrawTree.prototype.get_padding = function () { return this.padding };

Phylo.Forest.DrawTree.prototype.get_padding_top = function () { return this.padding_top };

Phylo.Forest.DrawTree.prototype.get_padding_bottom = function () { return this.padding_bottom };

Phylo.Forest.DrawTree.prototype.get_padding_left = function () { return this.padding_left };

Phylo.Forest.DrawTree.prototype.get_padding_right = function () { return this.padding_right };

Phylo.Forest.DrawTree.prototype.get_mode = function () { 
	if ( this.mode == null ) {
		this.mode = 'cladogram';
	} 
	return this.mode;
};

Phylo.Forest.DrawTree.prototype.get_shape = function () { return this.shape };

Phylo.Forest.DrawTree.prototype.get_text_horiz_offset = function () { return this.text_horiz_offset };

Phylo.Forest.DrawTree.prototype.get_text_vert_offset = function () { return this.text_vert_offset };

Phylo.Forest.DrawTree.prototype._redraw = function () {
	var tips_seen  = 0;
	var total_tips = this.calc_number_of_terminals();
	var tallest    = this.get_root().calc_max_path_to_tips();
	var maxnodes   = this.get_root().calc_max_nodes_to_tips();
	var is_clado   = this.get_mode().toLowerCase().indexOf('c') == 0 ? true : false;
	var height     = this.get_height();
	var width      = this.get_width();
	this.visit_depth_first({
		'post' : function (node) {
			var x;
			var y;
			if ( node.is_terminal() ) {
				tips_seen++;
				y = ( height / total_tips ) * tips_seen;
				x = is_clado 
					? width
					: ( width / tallest ) * node.calc_path_to_root();				
			}
			else {
				var children = node.get_children();
				y = 0;
				children.map( function(n) { y += n.y } );
				y /= children.length;
				x = is_clado 
					? width - ( ( width / maxnodes ) * node.calc_max_nodes_to_tips() )
					: ( width / tallest ) * node.calc_path_to_root();
			}
			node.set_y( y );
			node.set_x( x );
		}
	});	
};

Phylo.Forest.DrawTree.prototype._apply_to_nodes = function (property,value) {
	this.visit( function(n) { n[property] = value; } );	
};